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
package org.exist.xquery;

import org.exist.dom.persistent.*;
import org.exist.numbering.DLN;
import org.exist.numbering.NodeId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A node selector for following siblings.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class FollowingSiblingSelector implements NodeSelector {

  private final NodeSet contextSet;
  private final int contextId;
  private @Nullable final Expression expression;

//  private @Nullable NewArrayNodeSet precedingSiblings;
private @Nullable List<NodeProxy> precedingSiblings;

  public FollowingSiblingSelector(final NodeSet contextSet, final int contextId, @Nullable final Expression expression) {
    this.contextSet = contextSet;
    this.contextId = contextId;
    this.expression = expression;
  }

//  @Override
//  public @Nullable NodeProxy match(final DocumentImpl doc, final NodeId nodeId) {
//    final @Nullable NodeProxy precedingSibling = contextSet.containsPrecedingSiblingOf(doc, nodeId);
//    if (precedingSibling != null) {
//      final NodeProxy followingSibling = new NodeProxy(expression, doc, nodeId);
//
//      // START TEMP
////      followingSibling.addContextNode(contextId, new NodeProxy(expression, doc, new DLN("3.2")));
//      // END TEMP
//
//      if (Expression.IGNORE_CONTEXT != contextId) {
//        if (Expression.NO_CONTEXT_ID == contextId) {
//          followingSibling.copyContext(precedingSibling);
//        } else {
//          followingSibling.addContextNode(contextId, precedingSibling);
//        }
//      }
//      return followingSibling;
//    }
//    return null;
//  }

  @Override
  public @Nullable NodeProxy match(final DocumentImpl doc, final NodeId nodeId) {
    final @Nullable NodeProxy precedingSibling = contextSet.containsPrecedingSiblingOf(doc, nodeId);
    if (precedingSibling != null) {

      if (precedingSiblings == null) {
        // TODO(AR) do we need to sort this and remove duplicates, or is just a simple list enough?
//        precedingSiblings = new NewArrayNodeSet();
        this.precedingSiblings = new ArrayList<>();
      }
      precedingSiblings.add(precedingSibling);

      final NodeProxy followingSibling = new NodeProxy(expression, doc, nodeId);

//      System.out.println("currentId=" + followingSibling.getNodeId());

      if (Expression.IGNORE_CONTEXT != contextId) {
//        int i = 0;
        for (final NodeProxy contextNode : precedingSiblings) {
//          System.out.println("\t\tSTART INNER LOOP: " + i);

          // START TEMP
          if (followingSibling.getContext() == null || contextNode.getContext() == null
                  || followingSibling.getContext().getContextId() != contextNode.getContext().getContextId()) {

            if (Expression.NO_CONTEXT_ID == contextId) {
  //            System.out.println("\t\t\t" + followingSibling.getNodeId().toString() + " .copyContext(" + contextNode.getNodeId().toString() + ")");
              followingSibling.copyContext(contextNode);
            } else {
  //            System.out.println("\t\t\t" + followingSibling.getNodeId().toString() + " .addContextNode(" + contextId + ", " + contextNode.getNodeId().toString() + ")");
              followingSibling.addContextNode(contextId, contextNode);
            }

          } // END TEMP

//          System.out.println("\t\tEND INNER LOOP: " + i);
//          i++;
        }
      }

      return followingSibling;
    }
    return null;
  }
}
