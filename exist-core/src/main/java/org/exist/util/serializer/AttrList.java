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
package org.exist.util.serializer;

import org.exist.dom.persistent.AttrImpl;
import org.exist.dom.QName;
import org.exist.numbering.NodeId;

/**
 * Represents a list of attributes. Each attribute is defined by
 * a {@link org.exist.dom.QName} and a value. Instances
 * of this class can be passed to 
 * {@link org.exist.util.serializer.Receiver#startElement(QName, AttrList)}.
 * 
 * @author wolf
 */
public class AttrList {

    private NodeId[] nodeIds = new NodeId[4];
	private QName[] names = new QName[4];
	private String[] values = new String[4];
	private int[] type = new int[4];
	private int size = 0;

	public void addAttribute(final QName name, final String value) {
		addAttribute(name, value, AttrImpl.CDATA);
	}

	public void addAttributeNoIntern(final QName name, final String value) {
		addAttributeNoIntern(name, value, AttrImpl.CDATA);
	}

	public void addAttribute(final QName name, final String value, final int attrType) {
		addAttribute(name, value.intern(), attrType, null);
	}
	public void addAttributeNoIntern(final QName name, final String value, final int attrType) {
		addAttribute(name, value, attrType, null);
	}


	public void addAttribute(final QName name, final String value, final int attrType, final NodeId nodeId) {
		ensureCapacity();
        nodeIds[size] = nodeId;
		names[size] = name;
		values[size] = value;
        type[size] = attrType;
        size++;
	}
	
	public int getLength() {
		return size;
	}
	
	public QName getQName(final int pos) {
		return names[pos];
	}

    public NodeId getNodeId(final int pos) {
        return nodeIds[pos];
    }
    
	public String getValue(final int pos) {
		return values[pos];
	}
	
	public String getValue(final QName name) {
		for (int i = 0; i < size; i++) {
			if (names[i].equals(name)) {
				return values[i];
			}
		}
		return null;
	}

    public int getType(final int pos) {
        return type[pos];
    }
    
    private void ensureCapacity() {
		if (size == names.length) {
			// resize
			final int newSize = names.length * 3 / 2;
            final NodeId[] tnodeIds = new NodeId[newSize];
            System.arraycopy(nodeIds, 0, tnodeIds, 0, nodeIds.length);

			final QName[] tnames = new QName[newSize];
			System.arraycopy(names, 0, tnames, 0, names.length);
			
			final String[] tvalues = new String[newSize];
			System.arraycopy(values, 0, tvalues, 0, values.length);

            final int[] ttype = new int[newSize];
            System.arraycopy(type, 0, ttype, 0, type.length);

            nodeIds = tnodeIds;
            names = tnames;
			values = tvalues;
            type = ttype;
        }
	}
}
