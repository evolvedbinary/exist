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
package org.exist.util;

import net.jcip.annotations.ThreadSafe;

/**
 * A pool for char arrays.
 *
 * This pool is used by class XMLString. Whenever an XMLString needs to
 * reallocate the backing char[], the old array is released into the pool. However,
 * only char[] with length &lt; MAX are kept in the pool. Larger char[] are rarely reused.
 *
 * The pool is bound to the current thread.
 */
@ThreadSafe
public class CharArrayPool {

    private static final int POOL_SIZE = 128;
    private static final int MIN_SIZE = 8;

    private static final ThreadLocal<PoolContent> pools_ = new ThreadLocal<PoolContent>() {
        @Override
        protected PoolContent initialValue() {
            return new PoolContent();
        }
    };

    private CharArrayPool() {
    }

    public static char[] getCharArray(final int size) {
        final PoolContent poolContent = pools_.get();
        if (POOL_SIZE > size) {
            final int alloc = Math.max(size, MIN_SIZE);
            final char[][] pool = poolContent.pool;
            if (pool[alloc] != null) {
                final char[] b = pools_.get().pool[alloc];
                pool[alloc] = null;
                return b;
            }
        }
        return new char[Math.max(size, MIN_SIZE)];
    }

    public static void releaseCharArray(final char[] b) {
        if (b == null || b.length > POOL_SIZE) {
            return;
        }
        final PoolContent poolContent = pools_.get();
        poolContent.pool[b.length] = b;
     }

    private static class PoolContent {
        final char[][] pool = new char[POOL_SIZE][];
    }

}
