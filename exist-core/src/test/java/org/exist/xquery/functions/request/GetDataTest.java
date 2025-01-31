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
package org.exist.xquery.functions.request;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.exist.http.RESTTest;
import org.exist.xmldb.EXistResource;
import org.exist.xmldb.UserManagementService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author <a href="mailto:adam.retter@googlemail.com">Adam Retter</a>
 */
public class GetDataTest extends RESTTest {

    private static final String CONTAINER_ELEMENT_NAME = "data";
    private static final String XQUERY = wrapInElement("{request:get-data()}");
    private static final String XQUERY_FILENAME = "test-get-data.xq";

    private static Collection root;
    private static BasicHttpClientConnectionManager connectionManager;
    private static CloseableHttpClient client;

    private static String wrapInElement(final String value) {
        return value == null || value.isEmpty() ? "<" + CONTAINER_ELEMENT_NAME + "/>" : "<" + CONTAINER_ELEMENT_NAME + ">" + value + "</" + CONTAINER_ELEMENT_NAME + ">";
    }

    @BeforeClass
    public static void beforeClass() throws XMLDBException {
        root = DatabaseManager.getCollection("xmldb:exist://localhost:" + existWebServer.getPort() + "/xmlrpc/db", "admin", "");
        final BinaryResource res = root.createResource(XQUERY_FILENAME, BinaryResource.class);
        ((EXistResource) res).setMimeType("application/xquery");
        res.setContent(XQUERY);
        root.storeResource(res);
        final UserManagementService ums = root.getService(UserManagementService.class);
        ums.chmod(res, 0777);

        connectionManager = new BasicHttpClientConnectionManager();
        client = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build();
    }

    @AfterClass
    public static void afterClass() throws XMLDBException, IOException {
        final BinaryResource res = (BinaryResource) root.getResource(XQUERY_FILENAME);
        root.removeResource(res);
        client.close();
        connectionManager.close();
    }

    @Test
    public void retrieveEmpty() throws IOException {
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;
        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_0, (String) null, ContentType.APPLICATION_OCTET_STREAM);
        testRequest(post, wrapInElement("").getBytes());
    }

    @Test
    public void retrieveBinaryHttp09() throws IOException {
        final String testData = "12345";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_0_9, testData, ContentType.APPLICATION_OCTET_STREAM);
        try (final CloseableHttpResponse response = client.execute(post)) {
            assertEquals(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void retrieveBinaryHttp10() throws IOException {
        final String testData = "12345";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_0, testData, ContentType.APPLICATION_OCTET_STREAM);
        testRequest(post, wrapInElement(encodeBase64String(testData.getBytes(UTF_8)).trim()).getBytes());
    }

    @Test
    public void retrieveBinaryHttp11() throws IOException {
        final String testData = "12345";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_1, testData, ContentType.APPLICATION_OCTET_STREAM);
        testRequest(post, wrapInElement(encodeBase64String(testData.getBytes(UTF_8)).trim()).getBytes());
    }

    @Test
    public void retrieveBinaryHttp11ChunkedTransferEncoding() throws IOException {
        final String testData = "12345";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        try (final InputStream is = UnsynchronizedByteArrayInputStream.builder().setByteArray(testData.getBytes(UTF_8)).get()) {
            final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_1, is, ContentType.APPLICATION_OCTET_STREAM);
            testRequest(post, wrapInElement(encodeBase64String(testData.getBytes(UTF_8)).trim()).getBytes());
        }
    }

    @Test
    public void retrieveXmlHttp09() throws IOException {
        final String testData = "<a><b><c>hello</c></b></a>";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_0_9, testData, ContentType.TEXT_XML);
        try (final CloseableHttpResponse response = client.execute(post)) {
            assertEquals(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void retrieveXmlHttp10() throws IOException {
        final String testData = "<a><b><c>hello</c></b></a>";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_0, testData, ContentType.TEXT_XML);
        testRequest(post, wrapInElement("\n\t" + testData + "\n").getBytes(), true);
    }

    @Test
    public void retrieveXmlHttp11() throws IOException {
        final String testData = "<a><b><c>hello</c></b></a>";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_1, testData, ContentType.TEXT_XML);
        testRequest(post, wrapInElement("\n\t" + testData + "\n").getBytes(), true);
    }

    @Test
    public void retrieveXmlHttp11ChunkedTransferEncoding() throws IOException {
        final String testData = "<a><b><c>hello</c></b></a>";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        try (final InputStream is = UnsynchronizedByteArrayInputStream.builder().setByteArray(testData.getBytes(UTF_8)).get()) {
            final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_1, is, ContentType.TEXT_XML);
            testRequest(post, wrapInElement("\n\t" + testData + "\n").getBytes(), true);
        }
    }

    @Test
    public void retrieveMalformedXmlFallbackToString() throws IOException {
        final String testData = "<a><b></a>";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_0, testData, ContentType.TEXT_XML);
        testRequest(post, wrapInElement(testData.replace("<", "&lt;").replace(">", "&gt;")).getBytes());
    }

    @Test
    public void retrieveStringNoContentType() throws IOException {
        final String testData = "12345";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_0, new ByteArrayEntity(testData.getBytes(UTF_8)), null);
        testRequest(post, wrapInElement(testData).getBytes());
    }

    @Test
    public void retrieveStringTextContentType() throws IOException {
        final String testData = "12345";
        final String uri = getCollectionRootUri() + "/" + XQUERY_FILENAME;

        final HttpUriRequest post = createPostRequest(uri, HttpVersion.HTTP_1_0, testData, ContentType.TEXT_PLAIN);
        testRequest(post, wrapInElement(encodeBase64String(testData.getBytes(UTF_8)).trim()).getBytes());
    }

    private static HttpUriRequest createPostRequest(final String uri, final HttpVersion httpVersion, @Nullable final String body, final ContentType contentType) {
        final HttpEntity bodyEntity;
        if (body != null) {
            bodyEntity = new StringEntity(body, contentType);
        } else {
            bodyEntity = null;
        }
        return createPostRequest(uri, httpVersion, bodyEntity, contentType);
    }

    private static HttpUriRequest createPostRequest(final String uri, final HttpVersion httpVersion, @Nullable final InputStream body, final ContentType contentType) {
        final AbstractHttpEntity bodyEntity;
        if (body != null) {
            bodyEntity = new InputStreamEntity(body, contentType);
            bodyEntity.setChunked(true);
        } else {
            bodyEntity = null;
        }
        return createPostRequest(uri, httpVersion, bodyEntity, contentType);
    }

    private static HttpUriRequest createPostRequest(final String uri, final HttpVersion httpVersion, @Nullable final HttpEntity bodyEntity, final ContentType contentType) {
        final RequestBuilder builder = RequestBuilder.post(uri)
            .setVersion(httpVersion);

        if (bodyEntity == null) {
            builder.addHeader("Content-Type", contentType.getMimeType());
        } else {
            builder.setEntity(bodyEntity);
        }

        return builder.build();
    }

    private void testRequest(final HttpUriRequest request, final byte[] expectedResponse) throws IOException {
        testRequest(request, expectedResponse, false);
    }

    private void testRequest(final HttpUriRequest request, byte[] expectedResponse, final boolean stripWhitespaceAndFormatting) throws IOException {
        try (final CloseableHttpResponse response = client.execute(request)) {

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            try (final UnsynchronizedByteArrayOutputStream os = UnsynchronizedByteArrayOutputStream.builder().get()) {
                response.getEntity().writeTo(os);

                byte[] actualResponse = os.toByteArray();
                if (stripWhitespaceAndFormatting) {
                    expectedResponse = new String(expectedResponse).replace("\n", "").replace("\t", "").replace(" ", "").getBytes(UTF_8);
                    actualResponse = new String(actualResponse).replace("\n", "").replace("\t", "").replace(" ", "").getBytes(UTF_8);
                }
                assertArrayEquals(expectedResponse, actualResponse);
            }
        }
    }
}
