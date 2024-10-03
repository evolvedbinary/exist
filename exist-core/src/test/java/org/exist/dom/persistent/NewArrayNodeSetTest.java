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

package org.exist.dom.persistent;

import org.exist.numbering.DLN;
import org.exist.numbering.NodeId;
import org.exist.xquery.Constants;
import org.exist.xquery.value.SequenceIterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

import javax.annotation.Nullable;

import java.util.Iterator;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class NewArrayNodeSetTest {

    @Test
    public void iterate_loop() {
        final NewArrayNodeSet newArrayNodeSet = mockNewArrayNodeSet(99);

        final SequenceIterator it = newArrayNodeSet.iterate();
        int count = 0;
        while (it.hasNext()) {
            it.nextItem();
            count++;
        }

        assertEquals(99, count);
    }

    @Test
    public void iterate_skip_loop() {
        final NewArrayNodeSet newArrayNodeSet = mockNewArrayNodeSet(99);
        final SequenceIterator it = newArrayNodeSet.iterate();

        assertEquals(99, it.skippable());

        assertEquals(10, it.skip(10));

        assertEquals(89, it.skippable());

        int count = 0;
        while (it.hasNext()) {
            it.nextItem();
            count++;
        }

        assertEquals(89, count);
    }

    @Test
    public void iterate_loop_skip_loop() {
        final NewArrayNodeSet newArrayNodeSet = mockNewArrayNodeSet(99);
        final SequenceIterator it = newArrayNodeSet.iterate();

        int len = 20;
        int count = 0;
        for (int i = 0; it.hasNext() && i < len; i++) {
            it.nextItem();
            count++;
        }
        assertEquals(20, count);

        assertEquals(79, it.skippable());

        assertEquals(10, it.skip(10));

        assertEquals(69, it.skippable());

        count = 0;
        while (it.hasNext()) {
            it.nextItem();
            count++;
        }

        assertEquals(69, count);
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 3.8",
        "3.2, 3.4, 3.6, 3.8, 3.10",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14, 3.16",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18, 3.20",
    })
    public void containsPrecedingSiblingOf_sameLevel_sorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("3.8"));
        assertNotNull(nextPrecedingSibling);
        assertEquals(5, nextPrecedingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.6"), nextPrecedingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.6, 3.2, 3.8, 3.4",
        "3.10, 3.6, 3.2, 3.8, 3.4",
        "3.2, 3.12, 3.6, 3.8, 3.10, 3.4",
        "3.2, 3.4, 3.6, 3.10, 3.8, 3.12, 3.14",
        "3.2, 3.6, 3.4, 3.8, 3.16, 3.12, 3.14, 3.10",
        "3.10, 3.4, 3.12, 3.8, 3.6, 3.16, 3.2, 3.14, 3.18",
        "3.2, 3.8, 3.6, 3.10, 3.16, 3.4, 3.12, 3.14, 3.20, 3.18",
    })
    public void containsPrecedingSiblingOf_sameLevel_unsorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("3.8"));
        assertNotNull(nextPrecedingSibling);
        assertEquals(5, nextPrecedingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.6"), nextPrecedingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 3.8",
        "3.2, 3.4, 3.6, 3.8, 3.10",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14, 3.16",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18, 3.20",
    })
    public void containsPrecedingSiblingOf_sameLevel_sorted_duplicates(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("3.8"));
        assertNotNull(nextPrecedingSibling);
        assertEquals(5, nextPrecedingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.6"), nextPrecedingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.8, 3.2, 3.8, 3.4, 3.6",
        "3.2, 3.6, 3.4, 3.8, 3.6, 3.10",
        "3.6, 3.2, 3.4, 3.6, 3.8, 3.12, 3.8, 3.8, 3.10",
        "3.10, 3.12, 3.14, 3.2, 3.6, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8",
        "3.2, 3.8, 3.8, 3.4, 3.6, 3.8, 3.10, 3.12, 3.6, 3.14, 3.16",
        "3.2, 3.4, 3.8, 3.6, 3.6, 3.8, 3.10, 3.12, 3.8, 3.14, 3.16, 3.18",
        "3.2, 3.4, 3.6, 3.8, 3.6, 3.8, 3.10, 3.12, 3.8, 3.14, 3.16, 3.18, 3.20",
    })
    public void containsPrecedingSiblingOf_sameLevel_unsorted_duplicates(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("3.8"));
        assertNotNull(nextPrecedingSibling);
        assertEquals(5, nextPrecedingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.6"), nextPrecedingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 4.2",
        "3.2, 3.4, 3.6, 4.2, 4.4",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8, 4.10",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8, 4.10, 5.2",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8, 4.10, 5.2, 5.4",
    })
    public void containsPrecedingSiblingOf_differentLevels_sorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("4.2"));
        assertNull(nextPrecedingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "4.2, 3.4, 3.6, 3.2",
        "4.4, 4.2, 3.6, 3.2, 3.4",
        "3.2, 4.4, 3.6, 4.2, 3.4, 4.6",
        "3.2, 3.4, 4.8, 3.6, 4.2, 4.4, 4.6",
        "3.2, 3.4, 4.10, 4.2, 4.4, 3.6, 4.8, 4.12",
        "4.4, 4.6, 4.8, 4.10, 5.2, 3.2, 3.4, 3.6, 4.2",
        "4.4, 4.6, 4.8, 3.2, 3.4, 3.6, 4.2, 4.10, 5.2, 5.4",
    })
    public void containsPrecedingSiblingOf_differentLevels_unsorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("4.2"));
        assertNull(nextPrecedingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 4.2",
        "3.2, 3.4, 3.6, 4.2, 4.6",
        "3.2, 3.4, 3.6, 4.2, 4.6, 4.8",
    })
    public void containsPrecedingSiblingOf_none_sorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("3.2"));
        assertNull(nextPrecedingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 4.2, 3.4, 3.6",
        "3.2, 4.2, 3.6, 3.6, 4.4",
        "3.2, 4.8, 4.2, 3.6, 3.6, 4.4",
    })
    public void containsPrecedingSiblingOf_none_unsorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsPrecedingSiblingOf(mockDocument, new DLN("3.2"));
        assertNull(nextPrecedingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 3.8",
        "3.2, 3.4, 3.6, 3.8, 3.10",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14, 3.16",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18",
        "3.2, 3.4, 3.6, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18, 3.20",
    })
    public void containsFollowingSiblingOf_sameLevel_sorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextFollowingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("3.6"));
        assertNotNull(nextFollowingSibling);
        assertEquals(5, nextFollowingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.8"), nextFollowingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.6, 3.2, 3.8, 3.4",
        "3.10, 3.6, 3.2, 3.8, 3.4",
        "3.2, 3.12, 3.6, 3.8, 3.10, 3.4",
        "3.2, 3.4, 3.6, 3.10, 3.8, 3.12, 3.14",
        "3.2, 3.6, 3.4, 3.8, 3.16, 3.12, 3.14, 3.10",
        "3.10, 3.4, 3.12, 3.8, 3.6, 3.16, 3.2, 3.14, 3.18",
        "3.2, 3.8, 3.6, 3.10, 3.16, 3.4, 3.12, 3.14, 3.20, 3.18",
    })
    public void containsFollowingSiblingOf_sameLevel_unsorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextFollowingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("3.6"));
        assertNotNull(nextFollowingSibling);
        assertEquals(5, nextFollowingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.8"), nextFollowingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 3.8",
        "3.2, 3.4, 3.6, 3.8, 3.10",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14, 3.16",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18",
        "3.2, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8, 3.10, 3.12, 3.14, 3.16, 3.18, 3.20",
    })
    public void containsFollowingSiblingOf_sameLevel_sorted_duplicates(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextFollowingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("3.6"));
        assertNotNull(nextFollowingSibling);
        assertEquals(5, nextFollowingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.8"), nextFollowingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.8, 3.2, 3.8, 3.4, 3.6",
        "3.2, 3.6, 3.4, 3.8, 3.6, 3.10",
        "3.6, 3.2, 3.4, 3.6, 3.8, 3.12, 3.8, 3.8, 3.10",
        "3.10, 3.12, 3.14, 3.2, 3.6, 3.4, 3.6, 3.6, 3.8, 3.8, 3.8",
        "3.2, 3.8, 3.8, 3.4, 3.6, 3.8, 3.10, 3.12, 3.6, 3.14, 3.16",
        "3.2, 3.4, 3.8, 3.6, 3.6, 3.8, 3.10, 3.12, 3.8, 3.14, 3.16, 3.18",
        "3.2, 3.4, 3.6, 3.8, 3.6, 3.8, 3.10, 3.12, 3.8, 3.14, 3.16, 3.18, 3.20",
    })
    public void containsFollowingSiblingOf_sameLevel_unsorted_duplicates(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextFollowingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("3.6"));
        assertNotNull(nextFollowingSibling);
        assertEquals(5, nextFollowingSibling.getDoc().getDocId());
        assertEquals(new DLN("3.8"), nextFollowingSibling.getNodeId());
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 4.2",
        "3.2, 3.4, 3.6, 4.2, 4.4",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8, 4.10",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8, 4.10, 5.2",
        "3.2, 3.4, 3.6, 4.2, 4.4, 4.6, 4.8, 4.10, 5.2, 5.4",
    })
    public void containsFollowingSiblingOf_differentLevels_sorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextFollowingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("3.6"));
        assertNull(nextFollowingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "4.2, 3.4, 3.6, 3.2",
        "4.4, 4.2, 3.6, 3.2, 3.4",
        "3.2, 4.4, 3.6, 4.2, 3.4, 4.6",
        "3.2, 3.4, 4.8, 3.6, 4.2, 4.4, 4.6",
        "3.2, 3.4, 4.10, 4.2, 4.4, 3.6, 4.8, 4.12",
        "4.4, 4.6, 4.8, 4.10, 5.2, 3.2, 3.4, 3.6, 4.2",
        "4.4, 4.6, 4.8, 3.2, 3.4, 3.6, 4.2, 4.10, 5.2, 5.4",
    })
    public void containsFollowingSiblingOf_differentLevels_unsorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextFollowingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("3.6"));
        assertNull(nextFollowingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 3.4, 3.6, 4.2",
        "3.0, 3.2, 3.4, 3.6, 4.2",
        "3.0, 3.1, 3.2, 3.4, 3.6, 4.2",
    })
    public void containsFollowingSiblingOf_none_sorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("4.2"));
        assertNull(nextPrecedingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "3.2, 4.1, 3.4, 4.2",
        "3.2, 4.1, 3.6, 3.6, 4.2",
        "3.2, 4.1, 4.0, 3.6, 3.6, 4.2",
    })
    public void containsFollowingSiblingOf_none_unsorted(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        @Nullable final NodeProxy nextPrecedingSibling = newArrayNodeSet.containsFollowingSiblingOf(mockDocument, new DLN("4.2"));
        assertNull(nextPrecedingSibling);
    }

    @ParameterizedTest
    @CsvSource({
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 3.10, 3.12",
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 4.2, 4.4",
        "3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 3.10, 3.12, 3.4",
        "3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 4.2, 3.10, 3.12, 4.4, 3.4",
    })
    public void precedingSiblingsOf(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        final Iterator<NodeProxy> it = newArrayNodeSet.precedingSiblingsOf(mockDocument, new DLN("3.8"));

        for (final DLN expected : new DLN[] { new DLN("3.6"), new DLN("3.4"),  new DLN("3.2") }) {
            assertTrue(it.hasNext());
            final NodeId actual = it.next().getNodeId();
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 3.10, 3.12",
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 4.2, 4.4",
        "3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 3.10, 3.12, 3.4",
        "3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 4.2, 3.10, 3.12, 4.4, 3.4",
    })
    public void precedingSiblingsOfReverse(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        final Iterator<NodeProxy> it = newArrayNodeSet.precedingSiblingsOfReverse(mockDocument, new DLN("3.8"));

        for (final DLN expected : new DLN[] { new DLN("3.2"), new DLN("3.4"),  new DLN("3.6") }) {
            assertTrue(it.hasNext());
            final NodeId actual = it.next().getNodeId();
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 3.10, 3.12",
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 4.2, 4.4, 3.10, 3.12",
        "3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 3.10, 3.12, 3.4",
        "3.10, 3.12, 3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 4.2, 4.4, 3.4",
    })
    public void followingSiblingsOf(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        final Iterator<NodeProxy> it = newArrayNodeSet.followingSiblingsOf(mockDocument, new DLN("3.6"));

        for (final DLN expected : new DLN[] { new DLN("3.8"), new DLN("3.10"),  new DLN("3.12") }) {
            assertTrue(it.hasNext());
            final NodeId actual = it.next().getNodeId();
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 3.10, 3.12",
        "2.2, 2.4, 2.5, 3.2, 3.4, 3.6, 3.8, 4.2, 4.4, 3.10, 3.12",
        "3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 3.10, 3.12, 3.4",
        "3.10, 3.12, 3.2, 2.4, 2.5, 2.2, 3.6, 3.8, 4.2, 4.4, 3.4",
    })
    public void followingSiblingsOfReverse(final ArgumentsAccessor argumentsAccessor) {
        final DocumentImpl mockDocument = createMock(DocumentImpl.class);
        expect(mockDocument.getDocId()).andReturn(5).atLeastOnce();
        expect(mockDocument.getExpression()).andReturn(null).atLeastOnce();
        replay(mockDocument);

        final NewArrayNodeSet newArrayNodeSet = new NewArrayNodeSet();
        for (int i = 0; i < argumentsAccessor.size(); i++) {
            final String nodeIdStr = argumentsAccessor.getString(i);
            newArrayNodeSet.add(new NodeProxy(mockDocument, new DLN(nodeIdStr)));
        }

        final Iterator<NodeProxy> it = newArrayNodeSet.followingSiblingsOfReverse(mockDocument, new DLN("3.6"));

        for (final DLN expected : new DLN[] { new DLN("3.12"), new DLN("3.10"),  new DLN("3.8") }) {
            assertTrue(it.hasNext());
            final NodeId actual = it.next().getNodeId();
            assertEquals(expected, actual);
        }
    }

    private static NewArrayNodeSet mockNewArrayNodeSet(final int size) {
        final NodeProxy[] mockNodes = new NodeProxy[size];
        for (int i = 0; i < mockNodes.length; i++) {
            final NodeProxy mockNodeProxy = createMock(NodeProxy.class);
            replay(mockNodeProxy);
            mockNodes[i] = mockNodeProxy;
        }
        return new NewArrayNodeSetStub(mockNodes);
    }

    private static class NewArrayNodeSetStub extends NewArrayNodeSet {
        public NewArrayNodeSetStub(final NodeProxy... nodes) {
            for(final NodeProxy node : nodes) {
                addInternal(node, Constants.NO_SIZE_HINT);
            }
        }

        @Override
        public SequenceIterator iterate() {
            return new NewArrayIterator();
        }
    }
}
