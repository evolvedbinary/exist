/*
 * Copyright (c) 2018 Saxonica Limited.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
 */
package org.exist.util;

/**
 * This code was copied from Michael Kay's Saxon (see http://saxon.sf.net).
 */
public class SaxonURIUtil {

  /**
   * This function applies the URI escaping rules defined in section 2 of [RFC 2396] as amended by [RFC 2732],
   * with one exception, to the string supplied as $uri, which typically represents all or part of a URI.
   *
   * @param s the string to escape
   * @param escapeReserved  also escape reserved characters
   * @return the escaped uri string
   */
  public static String escape(CharSequence s, boolean escapeReserved) {
    final StringBuilder sb = new StringBuilder(s.length());
    for (int i=0; i<s.length(); i++) {
      final char c = s.charAt(i);
      if ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9')) {
        sb.append(c);
      } else if (c<=0x20 || c>=0x7f) {
        escapeChar(c, ((i+1)<s.length() ? s.charAt(i+1) : ' '), sb);
      } else if (escapeReserved) {
        if ("-_.!~*'()%".indexOf(c)>=0) {
          sb.append(c);
        } else {
          escapeChar(c, ' ', sb);
        }
      } else {
        if ("-_.!~*'()%;/?:@&=+$,#[]".indexOf(c)>=0) {
          sb.append(c);
        } else {
          escapeChar(c, ' ', sb);
        }
      }
    }
    return sb.toString();
  }

  private static final String hex = "0123456789ABCDEF";

  private static void escapeChar(char c, char c2, StringBuilder sb) {
    final byte[] array = new byte[4];
    final int used = UTF8.getUTF8Encoding(c, c2, array);
    for (int b=0; b<used; b++) {
      final int v = (array[b]>=0 ? array[b] : 256 + array[b]);
      sb.append('%');
      sb.append(hex.charAt(v/16));
      sb.append(hex.charAt(v%16));
    }
  }
}
