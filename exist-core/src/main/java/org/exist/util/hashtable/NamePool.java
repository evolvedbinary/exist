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
package org.exist.util.hashtable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;
import org.exist.dom.QName;
import org.exist.xquery.Constants;

/**
 * @author Pieter Deelen
 */
@ThreadSafe
public class NamePool {

    private final ConcurrentMap<WrappedQName, QName> pool;

    public NamePool() {
        pool = new ConcurrentHashMap<>();
    }

    private static String internNonNull(final String s) {
        return s == null ? null : s.intern();
    }

    public QName getSharedName(final QName name) {
        final WrappedQName wrapped = new WrappedQName(name);
        return pool.computeIfAbsent(wrapped, _key ->
            new QName(
                internNonNull(name.getLocalPart()),
                internNonNull(name.getNamespaceURI()),
                internNonNull(name.getPrefix()),
                name.getNameType()));
    }

    /**
     * QName ignores nameType and prefix when testing for equality.
     * Wrap it to overwrite those methods.
     */
    private static class WrappedQName implements Comparable<WrappedQName> {
        private final QName qname;

        public WrappedQName(final QName qname) {
            this.qname = qname;
        }

        @Override
        public int compareTo(final WrappedQName other) {
            if (qname.getNameType() != other.qname.getNameType()) {
                return qname.getNameType() < other.qname.getNameType() ? Constants.INFERIOR : Constants.SUPERIOR;
            }
            final int c;
            if (qname.getNamespaceURI() == null) {
                c = other.qname.getNamespaceURI() == null ? Constants.EQUAL : Constants.INFERIOR;
            } else if (other.qname.getNamespaceURI() == null) {
                c = Constants.SUPERIOR;
            } else {
                c = qname.getNamespaceURI().compareTo(other.qname.getNamespaceURI());
            }
            return c == Constants.EQUAL ? qname.getLocalPart().compareTo(other.qname.getLocalPart()) : c;
        }

        @Override
        public int hashCode() {
            int h = qname.getNameType() + 31 + qname.getLocalPart().hashCode();
            h += 31 * h + (qname.getNamespaceURI() == null ? 1 : qname.getNamespaceURI().hashCode());
            h += 31 * h + (qname.getPrefix() == null ? 1 : qname.getPrefix().hashCode());
            return h;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || !(obj instanceof WrappedQName)) {
                return false;
            }

            final WrappedQName other = (WrappedQName) obj;
            final int cmp = compareTo(other);
            if (cmp != 0) {
                return false;
            }

            if (qname.getPrefix() == null) {
                return other.qname.getPrefix() == null;
            } else if (other.qname.getPrefix() == null) {
                return false;
            } else {
                return qname.getPrefix().equals(other.qname.getPrefix());
            }
        }
    }
}
