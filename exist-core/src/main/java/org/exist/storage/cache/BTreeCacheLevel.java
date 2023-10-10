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
package org.exist.storage.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Iterator;

/**
 * A level of a BTree cache - either inner nodes, or outer nodes
 */
public class BTreeCacheLevel<T extends BTreeCacheable> extends LRUCache<T> {

    public BTreeCacheLevel(String name, int size, double growthFactor, double growthThreshold, CacheType type) {
        super(name, size, growthFactor, growthThreshold, type);
    }

    @Override
    public void add(final T item) {
        map.put(item.getKey(), item);
        if (map.size() >= max + 1) {
            removeNext(item);
        }
    }

    private void removeNext(final T item) {
        Iterator<Long2ObjectMap.Entry<T>> iterator = map.fastEntrySetIterator();
        while (iterator.hasNext()) {
            final Long2ObjectMap.Entry<T> next = iterator.next();
            final T cached = next.getValue();
            if(cached.allowUnload() && cached.getKey() != item.getKey()) {
                cached.sync(true);
                map.remove(next.getLongKey());

                // Continue if we are still oversize,
                // which might be possible if all entries were !allowUnload on a previous loop
                if (map.size() <= max) break;
            }
        }

        accounting.replacedPage(item);
        if (growthFactor > 1.0 && accounting.resizeNeeded()) {
            cacheManager.requestMem(this);
        }
    }
}
