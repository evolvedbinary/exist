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

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.dom.persistent.NodeSet;
import org.exist.numbering.NodeId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A node selector for preceding siblings.
 *
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public class PrecedingSiblingSelector implements NodeSelector {

  private final NodeSet contextSet;
  private final int contextId;
  private @Nullable final Expression expression;

//  private @Nullable List<NodeProxy> followingSiblings;
  private @Nullable List<NodeProxy> precedingSiblings;

  public PrecedingSiblingSelector(final NodeSet contextSet, final int contextId, @Nullable final Expression expression) {
    this.contextSet = contextSet;
    this.contextId = contextId;
    this.expression = expression;
  }

//  @Override
//  public @Nullable NodeProxy match(final DocumentImpl doc, final NodeId nodeId) {
//    final @Nullable NodeProxy followingSibling = contextSet.containsFollowingSiblingOf(doc, nodeId);
//    if (followingSibling != null) {
//      final NodeProxy precedingSibling = new NodeProxy(expression, doc, nodeId);
//      if (Expression.IGNORE_CONTEXT != contextId) {
//        if (Expression.NO_CONTEXT_ID == contextId) {
//          precedingSibling.copyContext(followingSibling);
//        } else {
//          precedingSibling.addContextNode(contextId, followingSibling);
//        }
//      }
//      return precedingSibling;
//    }
//    return null;
//  }

//  @Override
//  public @Nullable NodeProxy match(final DocumentImpl doc, final NodeId nodeId) {
//    final @Nullable NodeProxy followingSibling = contextSet.containsFollowingSiblingOf(doc, nodeId);
//    if (followingSibling != null) {
//
//      if (followingSiblings == null) {
//        this.followingSiblings = new ArrayList<>();
//      }
//      followingSiblings.add(followingSibling);
//
//      final NodeProxy precedingSibling = new NodeProxy(expression, doc, nodeId);
//
//      System.out.println("currentId=" + precedingSibling.getNodeId());
//
//      if (Expression.IGNORE_CONTEXT != contextId) {
//        int i = 0;
//        for (final NodeProxy contextNode : followingSiblings) {
//          System.out.println("\t\tSTART INNER LOOP: " + i);
//
//          if (Expression.NO_CONTEXT_ID == contextId) {
//            System.out.println("\t\t\t" + precedingSibling.getNodeId().toString() + " .copyContext(" + contextNode.getNodeId().toString() + ")");
//            precedingSibling.copyContext(contextNode);
//          } else {
//            System.out.println("\t\t\t" + precedingSibling.getNodeId().toString() + " .addContextNode(" + contextId + ", " + contextNode.getNodeId().toString() + ")");
//            precedingSibling.addContextNode(contextId, contextNode);
//          }
//
//          System.out.println("\t\tEND INNER LOOP: " + i);
//          i++;
//        }
//      }
//      return precedingSibling;
//    }
//    return null;
//  }

  @Override
  public @Nullable NodeProxy match(final DocumentImpl doc, final NodeId nodeId) {
    final @Nullable NodeProxy followingSibling = contextSet.containsFollowingSiblingOf(doc, nodeId);
    if (followingSibling != null) {

//      if (followingSiblings == null) {
//        this.followingSiblings = new ArrayList<>();
//      }
//      followingSiblings.add(followingSibling);

      final NodeProxy newPrecedingSibling = new NodeProxy(expression, doc, nodeId);

//      System.out.println("currentId=" + newPrecedingSibling.getNodeId());

      if (precedingSiblings == null) {
        this.precedingSiblings = new ArrayList<>();
      }
      precedingSiblings.add(newPrecedingSibling);

      if (Expression.IGNORE_CONTEXT != contextId) {
//        int i = 0;
        for (final NodeProxy precedingSibling : precedingSiblings) {
//          System.out.println("\t\tSTART INNER LOOP: " + i);

          if (Expression.NO_CONTEXT_ID == contextId) {
//            System.out.println("\t\t\t" + precedingSibling.getNodeId().toString() + " .copyContext(" + followingSibling.getNodeId().toString() + ")");
            precedingSibling.copyContext(followingSibling);
          } else {
//            System.out.println("\t\t\t" + precedingSibling.getNodeId().toString() + " .addContextNode(" + contextId + ", " + followingSibling.getNodeId().toString() + ")");
            precedingSibling.addContextNode(contextId, followingSibling);
          }

//          System.out.println("\t\tEND INNER LOOP: " + i);
//          i++;
        }
      }
      return newPrecedingSibling;
    }
    return null;
  }
}
