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
package org.exist.dom.persistent;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

/**
 * Iterate backward over a range of nodes.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class BackwardNodeRangeIterator implements NodeRangeIterator {
  @Nullable private NodeProxy[] nodes;
  private int idx;
  private int startIdx;

  public BackwardNodeRangeIterator() {
    this.nodes = null;
    this.idx = -1;
    this.startIdx = 0;
  }

  /**
   * @param nodes the nodes array.
   * @param startIdx the starting index within {@link #nodes}.
   * @param endIdx the ending index within {@link #nodes}.
   */
  public BackwardNodeRangeIterator(final NodeProxy[] nodes, final int startIdx, final int endIdx) {
    this.nodes = nodes;
    this.startIdx = startIdx;
    this.idx = endIdx;
  }

  @Override
  public boolean hasNext() {
    return idx >= startIdx;
  }

  @Override
  public NodeProxy next() {
    if (!hasNext() || nodes == null) {
      throw new NoSuchElementException();
    }
    return nodes[idx--];
  }

  @Override
  public void reset(final NodeProxy[] nodes, final int startIdx, final int endIdx) {
    this.nodes = nodes;
    this.startIdx = startIdx;
    this.idx = endIdx;
  }
}
