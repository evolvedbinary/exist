/*
 * Copyright (C) 2014, Evolved Binary Ltd
 *
 * This file was originally ported from FusionDB to eXist-db by
 * Evolved Binary, for the benefit of the eXist-db Open Source community.
 * Only the ported code as it appears in this file, at the time that
 * it was contributed to eXist-db, was re-licensed under The GNU
 * Lesser General Public License v2.1 only for use in eXist-db.
 *
 * This license grant applies only to a snapshot of the code as it
 * appeared when ported, it does not offer or infer any rights to either
 * updates of this source code or access to the original source code.
 *
 * The GNU Lesser General Public License v2.1 only license follows.
 *
 * ---------------------------------------------------------------------
 *
 * Copyright (C) 2014, Evolved Binary Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version 2.1.
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
package org.exist.xmldb;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.exist.util.Leasable;
import org.exist.util.SSLSelfSignedCertificateConnectionHelper;
import org.exist.xmlrpc.ExistRpcTypeFactory;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * XML:DB connection provider for connecting to an eXist-db instance via its XML-RPC API.
 *
 * The provider can use SSL if the "ssl-enable" property is set to "true".
 * If you are working with self-signed SSL certificates, it may also be
 * necessary to set the properties "ssl-allow-self-signed" and
 * "ssl-verify-hostname" to "true" and "false" respectively.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class XmlRpcDatabaseConnectionProvider implements DatabaseConnectionProvider {

  private static final String PROP_SSL_ENABLE = "ssl-enable";
  private static final String PROP_SSL_ALLOW_SELF_SIGNED = "ssl-allow-self-signed";
  private static final String PROP_SSL_VERIFY_HOSTNAME = "ssl-verify-hostname";

  // TODO(AR) does this need to be static? i.e. how many `XmlRpcDatabaseConnectionProvider` instances are there per DatabaseImpl
  private final Map<String, Leasable<XmlRpcClient>> rpcClients = new HashMap<>();

  @Override
  public boolean acceptsURI(@Nullable final Properties properties, final String uri) {
    @Nullable final XmldbURI xmldbUri = asXmlRpcXmldbUri(uri);
    return xmldbUri != null;
  }

  @Override
  public @Nullable Collection getCollection(@Nullable final Properties properties, final String uri, @Nullable String username, @Nullable String password) throws XMLDBException {
    @Nullable final XmldbURI xmldbUri = asXmlRpcXmldbUri(uri);
    if (xmldbUri == null) {
      throw new XMLDBException(ErrorCodes.INVALID_URI, "URI is not acceptable to this provider");
    }

    // if needed, use default username and password
    if (username == null) {
      username = "guest";
      password = "guest";
    }
    if (password == null) {
      password = "";
    }

    // if needed, configure SSL
    final boolean sslEnable = properties != null && "true".equalsIgnoreCase(properties.getProperty(PROP_SSL_ENABLE, "false"));
    if (sslEnable) {
      // TODO(AR) default for `sslAllowSelfSigned` should be `false` in future
      final boolean sslAllowSelfSigned = properties != null && "true".equalsIgnoreCase(properties.getProperty(PROP_SSL_ALLOW_SELF_SIGNED, "true"));
      // TODO(AR) default for `sslVerifyHostname` should be `true` in future
      final boolean sslVerifyHostname = properties != null && "true".equalsIgnoreCase(properties.getProperty(PROP_SSL_VERIFY_HOSTNAME, "false"));
      try {
        SSLSelfSignedCertificateConnectionHelper.getInstance(sslAllowSelfSigned, sslVerifyHostname);
      } catch (final NoSuchAlgorithmException | KeyManagementException e) {
        throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e);
      }
    }

    final String protocol = sslEnable ? "https" : "http";
    final URL url;
    try {
      url = new URL(protocol, xmldbUri.getHost(), xmldbUri.getPort(), xmldbUri.getContext());
    } catch (final MalformedURLException e) {
      throw new XMLDBException(ErrorCodes.INVALID_URI, e);
    }

    final Leasable<XmlRpcClient> rpcClient = getRpcClient(username, password, url);
    try {
      return readCollection(xmldbUri.getRawCollectionPath(), rpcClient);
    } catch (final XMLDBException e) {
      switch (e.errorCode) {
        case ErrorCodes.NO_SUCH_RESOURCE:
        case ErrorCodes.NO_SUCH_COLLECTION:
        case ErrorCodes.INVALID_COLLECTION:
        case ErrorCodes.INVALID_RESOURCE:
          return null;

        default:
          throw e;
      }
    }
  }

  private static @Nullable XmldbURI asXmlRpcXmldbUri(final String uri) {
    try {
      final String newUriString = XmldbURI.recoverPseudoURIs(uri);
      final XmldbURI xmldbUri = XmldbURI.xmldbUriFor(XmldbURI.XMLDB_URI_PREFIX + newUriString);
      if (XmldbURI.API_XMLRPC.equals(xmldbUri.getApiName())) {
        return xmldbUri;
      } else {
        return null;
      }
    } catch (final URISyntaxException e) {
      return null;
    }
  }

  private Leasable<XmlRpcClient> getRpcClient(final String user, final String password, final URL url) {
    return rpcClients.computeIfAbsent(rpcClientKey(user, url), key -> newRpcClient(user, password, url));
  }

  private String rpcClientKey(final String username, final URL url) {
    return username + "@" + url.toString();
  }

  private Leasable<XmlRpcClient> newRpcClient(final String username, final String password, final URL url) {
    final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setEnabledForExtensions(true);
    config.setContentLengthOptional(true);
    config.setGzipCompressing(true);
    config.setGzipRequesting(true);
    config.setServerURL(url);
    config.setBasicUserName(username);
    config.setBasicPassword(password);

    final XmlRpcClient client = new XmlRpcClient();
    client.setConfig(config);
    client.setTypeFactory(new ExistRpcTypeFactory(client));

    return new Leasable<>(client, _client -> rpcClients.remove(rpcClientKey(username, url)));
  }

  private static Collection readCollection(final String collectionPath, final Leasable<XmlRpcClient> rpcClient) throws XMLDBException {
    final XmldbURI path;
    try {
      path = XmldbURI.xmldbUriFor(collectionPath);
    } catch (final URISyntaxException e) {
      throw new XMLDBException(ErrorCodes.INVALID_URI, e);
    }

    final XmldbURI[] components = path.getPathSegments();
    if (components.length == 0) {
      throw new XMLDBException(ErrorCodes.NO_SUCH_COLLECTION, "Could not find collection: " + path);
    }

    XmldbURI rootName = components[0];
    if (XmldbURI.RELATIVE_ROOT_COLLECTION_URI.equals(rootName)) {
      rootName = XmldbURI.ROOT_COLLECTION_URI;
    }

    Collection current = RemoteCollection.instance(rpcClient, rootName);
    for (int i = 1; i < components.length; i++) {
      current = ((RemoteCollection) current).getChildCollection(components[i]);
      if (current == null) {
        throw new XMLDBException(ErrorCodes.NO_SUCH_COLLECTION, "Could not find collection: " + collectionPath);
      }
    }
    return current;
  }
}
