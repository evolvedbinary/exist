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
package org.exist.storage.dom;

import org.exist.dom.persistent.IStoredNode;
import org.exist.storage.btree.BTreeException;
import org.exist.storage.lock.ManagedLock;
import org.xml.sax.SAXException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public interface INodeIterator extends Iterator<IStoredNode>, Closeable {

    public ManagedLock<ReentrantLock> getReadLock();
}
