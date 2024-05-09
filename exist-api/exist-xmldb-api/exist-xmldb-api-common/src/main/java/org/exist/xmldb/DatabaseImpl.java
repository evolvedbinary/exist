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

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

import javax.annotation.Nullable;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * An XML:DB driver class for eXist-db. This driver is simply a centralised
 * facade to provide access to SPI implementations of {@link DatabaseConnectionProvider}.

 * The driver attempts to find a suitable  SPI implementation
 * for the XML:DB URI passed to {@link #acceptsURI(String)} or
 * {@link #getCollection(String, String, String)}.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class DatabaseImpl implements Database {

    public static final String DATABASE_ID = "database-id";
    public static final String CREATE_DATABASE = "create-database";

    private final Properties properties = new Properties();
    private final ServiceLoader<DatabaseConnectionProvider> databaseConnectionProviders;

    public DatabaseImpl() {
        this.properties.setProperty(CREATE_DATABASE, System.getProperty("exist.initdb", "false").toLowerCase());
        this.databaseConnectionProviders = ServiceLoader.load(DatabaseConnectionProvider.class);
    }

    @Override
    public String getConformanceLevel() {
        return "0";
    }

    @Override
    public String getName() {
        return properties.getProperty(DATABASE_ID, "exist");
    }

    @Override
    public String getProperty(final String name) {
        return properties.getProperty(name);
    }

    @Override
    public String getProperty(final String name, final String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    @Override
    public void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }

    @Override
    public boolean acceptsURI(final String uri) throws XMLDBException {
        return providerForUri(uri) != null;
    }

    /**
     * Determines if the URI is acceptable by the database.
     *
     * @param uri the URI.
     *
     * @return true if the URI us acceptable, false otherwise.
     *
     * @throws XMLDBException if an exception occurs.
     *
     * @deprecated Use {@link #acceptsURI(String)} instead.
     */
    @Deprecated
    public boolean acceptsURI(final XmldbURI uri)  throws XMLDBException {
        return acceptsURI(uri.toString());
    }

    @Override
    public Collection getCollection(final String uri, final String username, final String password) throws XMLDBException {
        @Nullable final DatabaseConnectionProvider databaseConnectionProvider = providerForUri(uri);
        if (databaseConnectionProvider == null) {
            throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED, "No org.exist.xmldb.DatabaseConnectionProvider that accepts URI like: " + uri);
        }
        return databaseConnectionProvider.getCollection(properties, uri, username, password);
    }

    /**
     * Retrieve the collection indicated by the URI.
     *
     * @param uri the URI.
     * @param username the username.
     * @param password the password.
     *
     * @return the collection or null.
     *
     * @throws XMLDBException if an exception occurs.
     *
     * @deprecated Use {@link #getCollection(String, String, String)} instead.
     */
    @Deprecated
    public @Nullable Collection getCollection(final XmldbURI uri, @Nullable final String username, @Nullable final String password) throws XMLDBException {
        return getCollection(uri.toString(), username, password);
    }

    private @Nullable DatabaseConnectionProvider providerForUri(final String uri) throws XMLDBException {
        for (final DatabaseConnectionProvider databaseConnectionProvider : databaseConnectionProviders) {
            if (databaseConnectionProvider.acceptsURI(properties, uri)) {
                return databaseConnectionProvider;
            }
        }
        return null;
    }
}
