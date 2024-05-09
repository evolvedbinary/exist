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
import org.xmldb.api.base.XMLDBException;

import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Interface for XML:DB connection providers.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public interface DatabaseConnectionProvider {

  /**
   * Determine whether this provider can handle the URI.
   *
   * @param properties any configuration properties that may be pertinent to this provider.
   * @param uri the URI to check for.
   * @return true if the URI can be handled by this provider, false otherwise.
   */
  boolean acceptsURI(@Nullable final Properties properties, final String uri);

  /**
   * Retrieve a collection identified by a URI.
   *
   * Authentication may be handled via username and password however it is not required that this
   * provider support authentication. Providers that do not support authentication MUST ignore the
   * {@code username} and {@code password}.
   *
   * @param properties any configuration properties that may be pertinent to this provider.
   * @param uri the URI that identifies the collection.
   * @param username The username to use for authentication to this provider.
   * @param password The password to use for authentication to this provider.
   * @return A {@code Collection} instance for the requested collection, or null if the collection
   *         could not be found.
   * @throws XMLDBException with expected error codes. {@code ErrorCodes.VENDOR_ERROR} for any
   *         vendor specific errors that occur. {@code ErrorCodes.INVALID_URI} If the URI is not in
   *         a valid format. {@code ErrorCodes.PERMISSION_DENIED} If the {@code username} and
   *         {@code password} were not accepted by the provider.
   */
  @Nullable Collection getCollection(@Nullable final Properties properties, final String uri, @Nullable final String username, @Nullable final String password) throws XMLDBException;
}
