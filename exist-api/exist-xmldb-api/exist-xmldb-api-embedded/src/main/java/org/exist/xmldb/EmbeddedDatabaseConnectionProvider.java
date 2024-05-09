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

import org.exist.EXistException;
import org.exist.security.AuthenticationException;
import org.exist.security.SecurityManager;
import org.exist.security.Subject;
import org.exist.storage.BrokerPool;
import org.exist.storage.journal.Journal;
import org.exist.util.Configuration;
import org.exist.util.DatabaseConfigurationException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

import javax.annotation.Nullable;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * XML:DB connection provider for an embedded (in JVM) eXist-db instance.
 *
 * The provider can create a new database instance if none is available yet.
 * It will do so if the property "create-database" is set to "true".
 *
 * You may optionally provide the location of an alternate configuration
 * file through the "configuration" property. Also the storage locations
 * for the database data files and journal can be specified
 * through the "data-dir" and "journal-dir" properties respectively.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class EmbeddedDatabaseConnectionProvider implements DatabaseConnectionProvider {

  private static final String PROP_CREATE_DATABASE = "create-database";
  private static final String PROP_CONFIGURATION = "configuration";
  private static final String PROP_DATA_DIR = "data-dir";
  private static final String PROP_JOURNAL_DIR = "journal-dir";

  @Override
  public boolean acceptsURI(@Nullable final Properties properties, final String uri) {
    @Nullable final XmldbURI xmldbUri = asEmbeddedXmldbUri(uri);
    return xmldbUri != null;
  }

  @Override
  public @Nullable Collection getCollection(@Nullable final Properties properties, final String uri, @Nullable final String username, @Nullable final String password) throws XMLDBException {
    @Nullable final XmldbURI xmldbUri = asEmbeddedXmldbUri(uri);
    if (xmldbUri == null) {
      throw new XMLDBException(ErrorCodes.INVALID_URI, "URI is not acceptable to this provider");
    }

    final String instanceName = xmldbUri.getInstanceName();
    try {
      final BrokerPool brokerPool;
      if (!BrokerPool.isConfigured(instanceName)) {
        final boolean createDatabase = properties != null && "true".equalsIgnoreCase(properties.getProperty(PROP_CREATE_DATABASE, "false"));
        if (createDatabase) {
          brokerPool = configureInstance(instanceName, properties);
        } else {
          throw new XMLDBException(ErrorCodes.INVALID_DATABASE, "Embedded database instance is not configured");
        }
      } else {
        brokerPool = BrokerPool.getInstance(instanceName);
      }

      final Subject authenticatedSubject = authenticateUser(brokerPool, username, password);

      return new LocalCollection(authenticatedSubject, brokerPool, xmldbUri.toCollectionPathURI());

    } catch (final DatabaseConfigurationException | EXistException e) {
      throw new XMLDBException(ErrorCodes.VENDOR_ERROR, "Failed to configure embedded database instance: " + instanceName, e);
    } catch (final AuthenticationException e) {
      throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, e);
    }
  }

  private static @Nullable XmldbURI asEmbeddedXmldbUri(final String uri) {
    try {
      final String newUriString = XmldbURI.recoverPseudoURIs(uri);
      final XmldbURI xmldbUri = XmldbURI.xmldbUriFor(XmldbURI.XMLDB_URI_PREFIX + newUriString);
      if (XmldbURI.API_LOCAL.equals(xmldbUri.getApiName())) {
        return xmldbUri;
      } else {
        return null;
      }
    } catch (final URISyntaxException e) {
      return null;
    }
  }

  private static BrokerPool configureInstance(final String instanceName, @Nullable final Properties properties) throws DatabaseConfigurationException, EXistException {
    @Nullable final String configurationFilePath = properties != null ? properties.getProperty(PROP_CONFIGURATION) : null;
    final Configuration config = new Configuration(configurationFilePath, Optional.empty());

    @Nullable final String dataDir = properties != null ? properties.getProperty(PROP_DATA_DIR) : null;
    if (dataDir != null) {
      config.setProperty(BrokerPool.PROPERTY_DATA_DIR, Paths.get(dataDir));
    }

    @Nullable final String journalDir = properties != null ? properties.getProperty(PROP_JOURNAL_DIR) : null;
    if (journalDir != null) {
      config.setProperty(Journal.PROPERTY_RECOVERY_JOURNAL_DIR, Paths.get(journalDir));
    }

    BrokerPool.configure(instanceName, 1, 5, config);

    return BrokerPool.getInstance(instanceName);
  }

  private static Subject authenticateUser(final BrokerPool brokerPool, @Nullable String username, @Nullable String password) throws AuthenticationException {
    if (username == null) {
      username = SecurityManager.GUEST_USER;
      password = SecurityManager.GUEST_USER;
    }

    final SecurityManager securityManager = brokerPool.getSecurityManager();
    return securityManager.authenticate(username, password);
  }
}
