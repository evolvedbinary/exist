/*
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
package org.exist.xmldb;

import org.exist.util.URIUtil;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utilities for XMLDB URI related functions.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class XmldbURIUtil {

  /**
   * This method decodes the provided uri for human readability.  The
   * method simply wraps URLDecoder.decode(uri,"UTF-8).  It is places here
   * to provide a friendly way to decode URIs encoded by urlEncodeUtf8()
   *
   * @param uri The uri to decode
   * @return The decoded value of the supplied uri
   */
  public static String urlDecodeUtf8(XmldbURI uri) {
    try {
      return URLDecoder.decode(uri.toString(), UTF_8.name());
    } catch(final UnsupportedEncodingException e) {
      //wrap with a runtime Exception
      throw new RuntimeException(e);
    }
  }

  /**
   * This method ensure that a collection path (e.g. /db/[])
   * is properly URL encoded.  Uses W3C recommended UTF-8
   * encoding.
   *
   * @param path The path to check
   * @return A UTF-8 URL encoded string
   */
  public static String ensureUrlEncodedUtf8(String path) {
    try {
      final XmldbURI uri = XmldbURI.xmldbUriFor(path);
      return uri.getRawCollectionPath();
    } catch (final URISyntaxException e) {
      return URIUtil.urlEncodePartsUtf8(path);
    }
  }

  /**
   * This method creates an <code>XmldbURI</code> by encoding the provided
   * string, then calling XmldbURI.xmldbUriFor(String) with the result of that
   * encoding
   *
   * @param path The path to encode and create an XmldbURI from
   * @return A UTF-8 URI encoded string
   * @throws URISyntaxException A URISyntaxException is thrown if the path
   * cannot be parsed by XmldbURI, after being encoded by
   * <code>urlEncodePartsUtf8</code>
   */
  public static XmldbURI encodeXmldbUriFor(String path) throws URISyntaxException {
    return XmldbURI.xmldbUriFor(URIUtil.urlEncodePartsUtf8(path));
  }
}
