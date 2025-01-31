/*
 * Copyright (C) 2014 Evolved Binary Ltd
 *
 * Changes made by Evolved Binary are proprietary and are not Open Source.
 *
 * NOTE: Parts of this file contain code from The eXist-db Authors.
 *       The original license header is included below.
 *
 * ----------------------------------------------------------------------------
 *
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery.modules.persistentlogin;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.exist.TestUtils;
import org.exist.test.ExistWebServer;
import org.exist.xmldb.EXistResource;
import org.exist.xmldb.UserManagementService;
import org.exist.xmldb.XmldbURI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;

public class LoginModuleTest {

    private static String XQUERY = "import module namespace login=\"http://exist-db.org/xquery/login\" " +
            "at \"resource:org/exist/xquery/modules/persistentlogin/login.xql\";" +
            "login:set-user('org.exist.login', (), false())," +
            "sm:id()/(descendant::sm:effective,descendant::sm:real)[1]/sm:username/string()";

    @ClassRule
    public static final ExistWebServer existWebServer = new ExistWebServer(true, false, true);

    private final static String XQUERY_FILENAME = "test-login.xql";

    private static Collection root;
    private static HttpClient client;

    @BeforeClass
    public static void beforeClass() throws XMLDBException {
        root = DatabaseManager.getCollection("xmldb:exist://localhost:" + existWebServer.getPort() + "/xmlrpc" + XmldbURI.ROOT_COLLECTION, TestUtils.ADMIN_DB_USER, TestUtils.ADMIN_DB_PWD);
        final BinaryResource res = root.createResource(XQUERY_FILENAME, BinaryResource.class);
        ((EXistResource) res).setMimeType("application/xquery");
        res.setContent(XQUERY);
        root.storeResource(res);
        final UserManagementService ums = root.getService(UserManagementService.class);
        ums.chmod(res, 0777);

        // NOTE(AR) Set client-side cookie handling to be RFC 6265 compliant, see: https://github.com/jetty/jetty.project/issues/12771
        final RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD)
            .build();

        final BasicCookieStore store = new BasicCookieStore();
        client = HttpClientBuilder.create()
            .setDefaultCookieStore(store)
            .setDefaultRequestConfig(defaultRequestConfig)
            .build();
    }

    @AfterClass
    public static void afterClass() throws XMLDBException {
        final BinaryResource res = (BinaryResource)root.getResource(XQUERY_FILENAME);
        root.removeResource(res);
    }

    @Test
    public void loginAndLogout() throws IOException {
        // not logged in
        String responseBody = doGet(null);
        assertEquals(TestUtils.GUEST_DB_USER, responseBody);

        // log in as admin
        responseBody = doGet("user=" + TestUtils.ADMIN_DB_USER + "&password=" + TestUtils.ADMIN_DB_PWD + "&duration=P1D");
        assertEquals(TestUtils.ADMIN_DB_USER, responseBody);

        // second request, 'admin' should have stayed logged in
        responseBody = doGet(null);
        assertEquals(TestUtils.ADMIN_DB_USER, responseBody);

        // log off, should return to guest user
        responseBody = doGet("logout=true");
        assertEquals(TestUtils.GUEST_DB_USER, responseBody);
    }

    private String doGet(final @Nullable String params) throws IOException {
        final HttpGet httpGet = new HttpGet("http://localhost:" + existWebServer.getPort() + "/rest" + XmldbURI.ROOT_COLLECTION + '/' + XQUERY_FILENAME +
                (params == null ? "" : "?" + params));
        HttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        final String responseBody = EntityUtils.toString(entity);
        assertEquals(responseBody, SC_OK, response.getStatusLine().getStatusCode());
        return responseBody;
    }
}
