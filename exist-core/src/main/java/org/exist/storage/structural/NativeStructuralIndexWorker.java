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
package org.exist.storage.structural;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.TypedQNameComparator;
import org.exist.dom.persistent.AttrImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.dom.QName;
import org.exist.dom.persistent.ElementImpl;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.IStoredNode;
import org.exist.dom.persistent.SymbolTable;
import org.exist.dom.persistent.NewArrayNodeSet;
import org.exist.dom.persistent.ExtNodeSet;
import org.exist.dom.persistent.NodeSet;
import org.exist.collections.Collection;
import org.exist.indexing.*;
import org.exist.indexing.StreamListener.ReindexMode;
import org.exist.numbering.NodeId;
import org.exist.storage.*;
import org.exist.storage.btree.BTree;
import org.exist.storage.btree.BTreeCallback;
import org.exist.storage.btree.IndexQuery;
import org.exist.storage.btree.Value;

import org.exist.storage.lock.ManagedLock;
import org.exist.storage.txn.Txn;
import org.exist.util.ByteConversion;
import org.exist.util.DatabaseConfigurationException;
import org.exist.util.LockException;
import org.exist.util.Occurrences;
import org.exist.xquery.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import org.exist.security.PermissionDeniedException;

import javax.annotation.Nullable;

/**
 * Internal default implementation of the structural index. It uses a single btree, in which
 * each key represents a sequence of: [type, qname, documentId, nodeId]. The btree value is just a
 * long pointing to the storage address of the actual node in dom.dbx.
 */
public class NativeStructuralIndexWorker implements IndexWorker, StructuralIndex {

    private final static Logger LOG = LogManager.getLogger(NativeStructuralIndexWorker.class);

    private final NativeStructuralIndex index;
    private ReindexMode mode = ReindexMode.STORE;
    private DocumentImpl document;
    private final NativeStructuralStreamListener listener = new NativeStructuralStreamListener();

    //TODO throw away this Comparator or use a different data struct here when we have moved
    //nameType out of QName
    private final Map<QName, List<NodeProxy>> pending = new TreeMap<>(new TypedQNameComparator());

    public NativeStructuralIndexWorker(final NativeStructuralIndex index) {
        this.index = index;
    }

    @Override
    public boolean matchElementsByTagName(final byte type, final DocumentSet docs, final QName qname, final NodeSelector selector) {
        return false;
    }

    @Override
    public boolean matchDescendantsByTagName(final byte type, final QName qname, final int axis, final DocumentSet docs, final ExtNodeSet contextSet, final int contextId) {
        return false;
    }

    /**
     * Find all nodes in the index matching a given QName. If a match is selected and returned depends on
     * the specified {@link org.exist.xquery.NodeSelector}.
     *
     * This implementation does a scan through the index for a range of document ids in the input set.
     * It will be fast for bulk-loading a large node set, but slow if you need to operate on a small
     * context set.
     *
     * @param type the type of the element
     * @param docs the document working set
     * @param qname the name
     * @param selector the selector
     * @return nodeset the matching nodeset
     */
    @Override
    public NodeSet findElementsByTagName(final byte type, final DocumentSet docs, final QName qname, @Nullable final NodeSelector selector) {
        return findElementsByTagName(type, docs, qname, selector, null);
    }

    @Override
    public NodeSet findElementsByTagName(final byte type, final DocumentSet docs, final QName qname, @Nullable final NodeSelector selector, @Nullable final Expression parent) {
        final NewArrayNodeSet result = new NewArrayNodeSet();
        final FindElementsCallback callback = new FindElementsCallback(type, qname, result, docs, selector, parent);

        // for each document id range, scan the index to find matches
        for (final Range range : getDocIdRanges(docs)) {
            final byte[] fromKey = computeKey(type, qname, range.start);
            final byte[] toKey = computeKey(type, qname, range.end + 1);
            final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));

            try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeReadLock(index.btree.getLockName())) {
                index.btree.query(query, callback);
            } catch (final LockException e) {
                NativeStructuralIndex.LOG.warn("Lock problem while searching structural index: {}", e.getMessage(), e);
            } catch (final TerminatedException e) {
                NativeStructuralIndex.LOG.warn("Query was terminated while searching structural index: {}", e.getMessage(), e);
            } catch (final Exception e) {
                NativeStructuralIndex.LOG.error("Error while searching structural index: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * Scan the document set to find document id ranges to query
     *
     * @param docs the document set
     * @return List of contiguous document id ranges
     */
    List<Range> getDocIdRanges(final DocumentSet docs) {
        final List<Range> ranges = new ArrayList<>();
        Range next = null;
        for (final Iterator<DocumentImpl> i = docs.getDocumentIterator(); i.hasNext(); ) {
            final DocumentImpl doc = i.next();
            if (next == null) {
                next = new Range(doc.getDocId());
            } else if (next.end + 1 == doc.getDocId()) {
                next.end++;
            } else {
                ranges.add(next);
                next = new Range(doc.getDocId());
            }
        }
        if (next != null) {
            ranges.add(next);
        }

        return ranges;
    }

    /**
     * Internal helper class used by
     * {@link NativeStructuralIndexWorker#findElementsByTagName(byte, org.exist.dom.persistent.DocumentSet, org.exist.dom.QName, org.exist.xquery.NodeSelector)}.
     */
    static class Range {
        final int start;
        int end;

        private Range(final int start) {
            this.start = start;
            this.end = start;
        }
    }
    
    /**
     * Find all descendants (or children) of the specified node set matching the given QName.
     *
     * This implementation does one btree lookup for every node in contextSet. It offers superior performance
     * if the number of nodes in contextSet is rather small compared to the overall number of nodes in
     * the index.
     * @param type the type of node to find
     * @param qname the node name
     * @param axis the node axis
     * @param docs the document set
     * @param contextSet the context set
     * @param contextId the id of the context
     *
     * @return the matching decendants
     *
     */
    @Override
    public NodeSet findDescendantsByTagName(final byte type, final QName qname, final int axis, final DocumentSet docs, final NodeSet contextSet, final int contextId) {
        return findDescendantsByTagName(type, qname, axis, docs, contextSet, contextId, null);
    }

    @Override
    public NodeSet findDescendantsByTagName(final byte type, final QName qname, final int axis, final DocumentSet docs, final NodeSet contextSet, final int contextId, @Nullable final Expression parent) {
        final NewArrayNodeSet result = new NewArrayNodeSet();
        final FindDescendantsCallback callback = new FindDescendantsCallback(type, axis, qname, contextId, result, parent);
        try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeReadLock(index.btree.getLockName())) {
            for (final NodeProxy ancestor : contextSet) {
                final DocumentImpl doc = ancestor.getOwnerDocument();
                final NodeId ancestorId = ancestor.getNodeId();
                callback.setAncestor(doc, ancestor);
                final byte[] fromKey;
                final byte[] toKey;
                if (ancestorId == NodeId.DOCUMENT_NODE) {
                    fromKey = computeKey(type, qname, doc.getDocId());
                    toKey = computeKey(type, qname, doc.getDocId() + 1);
                } else {
                    fromKey = computeKey(type, qname, doc.getDocId(), ancestorId);
                    toKey = computeKey(type, qname, doc.getDocId(), ancestorId.nextSibling());
                }
                final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));
                try {
                    index.btree.query(query, callback);
                } catch (final Exception e) {
                    NativeStructuralIndex.LOG.error("Error while searching structural index: {}", e.getMessage(), e);
                }
            }
        } catch (final LockException e) {
            NativeStructuralIndex.LOG.warn("Lock problem while searching structural index: {}", e.getMessage(), e);
        }
        result.updateNoSort();
        return result;
    }

    @Override
    public NodeSet findAncestorsByTagName(final byte type, final QName qname, final int axis, final DocumentSet docs, final NodeSet contextSet, final int contextId) {
        final NewArrayNodeSet result = new NewArrayNodeSet();
        try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeReadLock(index.btree.getLockName())) {
            for (final NodeProxy descendant : contextSet) {
                NodeId parentId;
                if (axis == Constants.ANCESTOR_SELF_AXIS || axis == Constants.SELF_AXIS) {
                    parentId = descendant.getNodeId();
                } else {
                    parentId = descendant.getNodeId().getParentId();
                }
                final DocumentImpl doc = descendant.getOwnerDocument();
                while (parentId != NodeId.DOCUMENT_NODE) {
                    final byte[] key = computeKey(type, qname, doc.getDocId(), parentId);
                    final long address = index.btree.findValue(new Value(key));
                    if (address != -1) {
                        final NodeProxy storedNode = new NodeProxy(null, doc, parentId,
                            type == ElementValue.ATTRIBUTE ? Node.ATTRIBUTE_NODE : Node.ELEMENT_NODE, address);
                        result.add(storedNode);
                        if (Expression.NO_CONTEXT_ID != contextId) {
                            storedNode.deepCopyContext(descendant, contextId);
                        } else {
                            storedNode.copyContext(descendant);
                        }
                        if (contextSet.getTrackMatches()) {
                            storedNode.addMatches(descendant);
                        }
                    }
                    // stop after first iteration if we are on the self axis
                    if (axis == Constants.SELF_AXIS || axis == Constants.PARENT_AXIS) {
                        break;
                    }
                    // continue with the parent of the parent
                    parentId = parentId.getParentId();
                }
            }
        } catch (final LockException e) {
            NativeStructuralIndex.LOG.warn("Lock problem while searching structural index: {}", e.getMessage(), e);
        } catch (final Exception e) {
            NativeStructuralIndex.LOG.error("Error while searching structural index: {}", e.getMessage(), e);
        }
        result.sort(true);
        return result;
    }

    @Override
    public NodeSet scanByType(final byte type, final int axis, final NodeTest test, final boolean useSelfAsContext, final DocumentSet docs, final NodeSet contextSet, final int contextId) {
        final NewArrayNodeSet result = new NewArrayNodeSet();
        final FindDescendantsCallback callback = new FindDescendantsCallback(type, axis, null, contextId, useSelfAsContext, result, null);
        for (final NodeProxy ancestor : contextSet) {
            final DocumentImpl doc = ancestor.getOwnerDocument();
            final NodeId ancestorId = ancestor.getNodeId();
            final List<QName> qnames = getQNamesForDoc(doc);
            try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeReadLock(index.btree.getLockName())) {
              for (final QName qname : qnames) {
                if (test.getName() == null || test.matches(qname)) {
                  callback.setAncestor(doc, ancestor);
                  final byte[] fromKey, toKey;
                      if (ancestorId == NodeId.DOCUMENT_NODE) {
                          fromKey = computeKey(type, qname, doc.getDocId());
                          toKey = computeKey(type, qname, doc.getDocId() + 1);
                      } else {
                          fromKey = computeKey(type, qname, doc.getDocId(), ancestorId);
                          toKey = computeKey(type, qname, doc.getDocId(), ancestorId.nextSibling());
                      }
                      final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));
                      try {
                          index.btree.query(query, callback);
                      } catch (final Exception e) {
                            NativeStructuralIndex.LOG.error("Error while searching structural index: {}", e.getMessage(), e);
                      }
                }
              }
            } catch (final LockException e) {
                NativeStructuralIndex.LOG.warn("Lock problem while searching structural index: {}", e.getMessage(), e);
            }
        }
//        result.updateNoSort();
        return result;
    }
    
    private class FindElementsCallback implements BTreeCallback {
        private final byte type;
        private @Nullable final QName qname;
        private final DocumentSet docs;
        private final NewArrayNodeSet result;
        private @Nullable final NodeSelector selector;
        private @Nullable final Expression parent;

        FindElementsCallback(final byte type, final QName qname, final NewArrayNodeSet result, final DocumentSet docs, @Nullable final NodeSelector selector, @Nullable final Expression parent) {
            this.type = type;
            this.result = result;
            this.docs = docs;
            this.selector = selector;
            this.parent = parent;
            if (qname != null && qname.getNameType() != type) {
                this.qname = new QName(qname.getLocalPart(), qname.getNamespaceURI(), qname.getPrefix(), type);
            } else {
                this.qname = qname;
            }
        }

        @Override
        public boolean indexInfo(final Value value, final long pointer) throws TerminatedException {
            if (parent != null) {
                parent.getContext().proceed(parent);
            }
            final byte[] key = value.getData();
            final NodeId nodeId = readNodeId(key, pointer);
            final DocumentImpl doc = docs.getDoc(readDocId(key));
            if (doc != null) {
                if (selector == null) {
                    final NodeProxy storedNode = new NodeProxy(null, doc, nodeId,
                        type == ElementValue.ATTRIBUTE ? Node.ATTRIBUTE_NODE : Node.ELEMENT_NODE, pointer);
                    if (qname != null) {
                        storedNode.setQName(qname);
                    }
                    result.add(storedNode);
                } else {
                    final NodeProxy storedNode = selector.match(doc, nodeId);
                    if (storedNode != null) {
                        storedNode.setNodeType(type == ElementValue.ATTRIBUTE ? Node.ATTRIBUTE_NODE : Node.ELEMENT_NODE);
                        storedNode.setInternalAddress(pointer);
                        if (qname != null) {
                            storedNode.setQName(qname);
                        }
                        result.add(storedNode);
                    }
                }
            }
            return true;
        }
    }

    private class FindDescendantsCallback implements BTreeCallback {
        private final int axis;
        private final byte type;
        private final QName qname;
        private NodeProxy ancestor;
        private DocumentImpl doc;
        private final int contextId;
        private final NewArrayNodeSet result;
        private final boolean selfAsContext;
        private final Expression parent;

        FindDescendantsCallback(final byte type, final int axis, final QName qname, final int contextId, final NewArrayNodeSet result, @Nullable final Expression parent) {
        	this(type, axis, qname, contextId, false, result, parent);
        }
        
        FindDescendantsCallback(final byte type, final int axis, final QName qname, final int contextId, final boolean selfAsContext, final NewArrayNodeSet result, @Nullable Expression parent) {
            this.type = type;
            this.axis = axis;
            this.contextId = contextId;
            this.result = result;
            this.selfAsContext = selfAsContext;
            this.parent = parent;
            if (qname != null && qname.getNameType() != type) {
                this.qname = new QName(qname.getLocalPart(), qname.getNamespaceURI(), qname.getPrefix(), type);
            } else {
                this.qname = qname;
            }
        }

        void setAncestor(final DocumentImpl doc, final NodeProxy ancestor) {
            this.doc = doc;
            this.ancestor = ancestor;
        }

        @Override
        public boolean indexInfo(final Value value, final long pointer) throws TerminatedException {
            if (parent != null) {
                parent.getContext().proceed(parent);
            }
            final NodeId nodeId = readNodeId(value.getData(), pointer);

            boolean match = axis == Constants.DESCENDANT_SELF_AXIS || axis == Constants.DESCENDANT_ATTRIBUTE_AXIS;
            if (!match) {
                final int relation = nodeId.computeRelation(ancestor.getNodeId());
                match = (((axis == Constants.CHILD_AXIS) || (axis == Constants.ATTRIBUTE_AXIS)) && (relation == NodeId.IS_CHILD)) ||
                    ((axis == Constants.DESCENDANT_AXIS) && ((relation == NodeId.IS_DESCENDANT) || (relation == NodeId.IS_CHILD)));
            }
            if (match) {
                final NodeProxy storedNode =
                    new NodeProxy(null, doc, nodeId, type == ElementValue.ATTRIBUTE ? Node.ATTRIBUTE_NODE : Node.ELEMENT_NODE, pointer);
                if (qname != null) {
                    storedNode.setQName(qname);
                }
                result.add(storedNode);
                if (Expression.NO_CONTEXT_ID != contextId) {
                	if (selfAsContext)
                		{storedNode.addContextNode(contextId, storedNode);}
                	else
                		{storedNode.deepCopyContext(ancestor, contextId);}
                } else {
            		storedNode.copyContext(ancestor);
                }
                storedNode.addMatches(ancestor);
            }
            return true;
        }
    }

    @Override
    public String getIndexId() {
        return NativeStructuralIndex.ID;
    }

    @Override
    public String getIndexName() {
        return index.getIndexName();
    }

    @Override
    public Object configure(final IndexController controller, final NodeList configNodes, final Map<String, String> namespaces) throws DatabaseConfigurationException {
        return null;
    }

    @Override
    public void setDocument(final DocumentImpl doc) {
        setDocument(doc, ReindexMode.UNKNOWN);
    }

    @Override
    public void setDocument(final DocumentImpl doc, final ReindexMode mode) {
        this.document = doc;
        this.mode = mode;
    }

    @Override
    public void setMode(final ReindexMode mode) {
        this.mode = mode;
    }

    @Override
    public DocumentImpl getDocument() {
        return document;
    }

    @Override
    public ReindexMode getMode() {
        return mode;
    }

    @Override
    public <T extends IStoredNode> IStoredNode getReindexRoot(final IStoredNode<T> node, final NodePath path, final boolean insert, final boolean includeSelf) {
        // if a node is inserted, we do not need to reindex the parent
        return insert ? null : node;
    }

    @Override
    public StreamListener getListener() {
        return listener;
    }

    @Override
    public MatchListener getMatchListener(final DBBroker broker, final NodeProxy proxy) {
        // not applicable to this index
        return null;
    }

    @Override
    public void flush() {
        switch (mode) {
            case STORE:
                processPending();
                break;
            case REMOVE_ALL_NODES:
                removeDocument(document);
                break;
            case REMOVE_SOME_NODES:
                removeSome();
                break;
        }
    }

    protected void removeSome() {
        if (pending.isEmpty()) {
            return;
        }

        try {
            for (final Map.Entry<QName,List<NodeProxy>> entry: pending.entrySet()) {
                final QName qname = entry.getKey();
                try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeWriteLock(index.btree.getLockName())) {
                    final List<NodeProxy> nodes = entry.getValue();
                    for (final NodeProxy proxy : nodes) {
                        final NodeId nodeId = proxy.getNodeId();
                        final byte[] key = computeKey(qname.getNameType(), qname, document.getDocId(), nodeId);
                        index.btree.removeValue(new Value(key));
                    }
                } catch (final LockException e) {
                    NativeStructuralIndex.LOG.warn("Failed to lock structural index: {}", e.getMessage(), e);
                } catch (final Exception e) {
                    NativeStructuralIndex.LOG.warn("Exception caught while writing to structural index: {}", e.getMessage(), e);
                }
            }
        } finally {
            pending.clear();
        }
    }

    protected void removeDocument(final DocumentImpl docToRemove) {
        if (index.btree == null) {
            return;
        }
        final List<QName> qnames = getQNamesForDoc(docToRemove);
        for (final QName qname : qnames) {
            final byte[] fromKey = computeKey(qname.getNameType(), qname, docToRemove.getDocId());
            final byte[] toKey = computeKey(qname.getNameType(), qname, docToRemove.getDocId() + 1);
            final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));
            try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeWriteLock(index.btree.getLockName())) {
                index.btree.remove(query, null);
            } catch (final LockException e) {
                NativeStructuralIndex.LOG.warn("Failed to lock structural index: {}", e.getMessage(), e);
            } catch (final Exception e) {
                NativeStructuralIndex.LOG.warn("Exception caught while removing structural index for document {}: {}", docToRemove.getURI(), e.getMessage(), e);
            }
        }
        removeQNamesForDoc(docToRemove);
    }

    protected void removeQNamesForDoc(final DocumentImpl doc) {
        final byte[] fromKey = computeDocKey(doc.getDocId());
        final byte[] toKey = computeDocKey(doc.getDocId() + 1);
        final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));
        try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeWriteLock(index.btree.getLockName())) {
            index.btree.remove(query, null);
        } catch (final LockException e) {
            NativeStructuralIndex.LOG.warn("Failed to lock structural index: {}", e.getMessage(), e);
        } catch (final Exception e) {
            NativeStructuralIndex.LOG.warn("Exception caught while reading structural index for document {}: {}", doc.getURI(), e.getMessage(), e);
        }
    }

    protected List<QName> getQNamesForDoc(final DocumentImpl doc) {
        final List<QName> qnames = new ArrayList<>();
        if (index.btree == null) {
            return qnames;
        }
        final byte[] fromKey = computeDocKey(doc.getDocId());
        final byte[] toKey = computeDocKey(doc.getDocId() + 1);
        final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));
        try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeWriteLock(index.btree.getLockName())) {
            index.btree.query(query, (value, pointer) -> {
                final QName qname = readQName(value.getData());
                qnames.add(qname);
                return true;
            });
        } catch (final LockException e) {
            NativeStructuralIndex.LOG.warn("Failed to lock structural index: {}", e.getMessage(), e);
        } catch (final Exception e) {
            NativeStructuralIndex.LOG.warn("Exception caught while reading structural index for document {}: {}", doc.getURI(), e.getMessage(), e);
        }
        return qnames;
    }

    @Override
    public void removeCollection(final Collection collection, final DBBroker broker, final boolean reindex) throws PermissionDeniedException {
        try {
            for (final Iterator<DocumentImpl> i = collection.iterator(broker); i.hasNext(); ) {
                final DocumentImpl doc = i.next();
                removeDocument(doc);
            }
        } catch(final LockException e) {
            LOG.error(e);
        }
    }

    @Override
    public boolean checkIndex(final DBBroker broker) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Collect index statistics. Used by functions like util:index-keys.
     *
     * @param context the xquery context
     * @param docs The documents to which the index entries belong
     * @param contextSet ignored by this index
     * @param hints Some "hints" for retrieving the index entries. See such hints in
     * {@link org.exist.indexing.OrderedValuesIndex} and {@link org.exist.indexing.QNamedKeysIndex}.
     * @return the matching occurrences
     */
    @Override
    public Occurrences[] scanIndex(final XQueryContext context, final DocumentSet docs, final NodeSet contextSet, final Map<?, ?> hints) {
        final Map<String, Occurrences> occurrences = new TreeMap<>();
        for (final Iterator<DocumentImpl> i = docs.getDocumentIterator(); i.hasNext(); ) {
            final DocumentImpl doc = i.next();
            final List<QName> qnames = getQNamesForDoc(doc);
            for (final QName qname : qnames) {
                final String name;
                if (qname.getNameType() == ElementValue.ATTRIBUTE) {
                    name = "@" + qname.getLocalPart();
                } else {
                    name = qname.getLocalPart();
                }
                final byte[] fromKey = computeKey(qname.getNameType(), qname, doc.getDocId());
                final byte[] toKey = computeKey(qname.getNameType(), qname, doc.getDocId() + 1);
                final IndexQuery query = new IndexQuery(IndexQuery.RANGE, new Value(fromKey), new Value(toKey));

                try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeReadLock(index.btree.getLockName())) {
                    index.btree.query(query, (value, pointer) -> {
                        Occurrences oc = occurrences.get(name);
                        if (oc == null) {
                            oc = new Occurrences(name);
                            occurrences.put(name, oc);
                            oc.addDocument(doc);
                            oc.addOccurrences(1);
                        } else {
                            oc.addOccurrences(1);
                            oc.addDocument(doc);
                        }
                        return true;
                    });
                } catch (final LockException e) {
                    NativeStructuralIndex.LOG.warn("Failed to lock structural index: {}", e.getMessage(), e);
                } catch (final Exception e) {
                    NativeStructuralIndex.LOG.warn("Exception caught while reading structural index for document {}: {}", doc.getURI(), e.getMessage(), e);
                }
            }
        }
        final Occurrences[] result = new Occurrences[occurrences.size()];
        int i = 0;
        for (Occurrences occ: occurrences.values()) {
            result[i++] = occ;
        }
        return result;
    }

    @Override
    public QueryRewriter getQueryRewriter(final XQueryContext context) {
        return null;
    }

    public BTree getStorage() {
        return index.btree;
    }

    private void addNode(final QName qname, final NodeProxy proxy) {
        if (document.getDocId() != proxy.getOwnerDocument().getDocId()) {
    		throw new IllegalArgumentException("Document id ('" + document.getDocId() + "') and proxy id ('" +
            proxy.getOwnerDocument().getDocId() + "') differ !");
        }
        //Is this qname already pending ? Create a node list when not present.
        final List<NodeProxy> buf = pending.computeIfAbsent(qname, k -> new ArrayList<>(50));

        //Add node's proxy to the list
        buf.add(proxy);
    }

    /**
     * Process the map of pending entries and store them into the btree.
     */
    private void processPending() {
        if (pending.isEmpty() || index.btree == null) {
            return;
        }

        try {
            for (final Map.Entry<QName,List<NodeProxy>> entry: pending.entrySet()) {
                final QName qname = entry.getKey();
                try(final ManagedLock<ReentrantLock> btreeLock = index.lockManager.acquireBtreeWriteLock(index.btree.getLockName())) {
                    final List<NodeProxy> nodes = entry.getValue();
                    for (final NodeProxy proxy : nodes) {
                        final NodeId nodeId = proxy.getNodeId();
                        final byte[] key = computeKey(qname.getNameType(), qname, document.getDocId(), nodeId);
                        index.btree.addValue(new Value(key), computeValue(proxy));
                    }
                    final Value docKey = new Value(computeDocKey(qname.getNameType(), document.getDocId(), qname));
                    if (index.btree.findValue(docKey) == -1) {
                        index.btree.addValue(docKey, 0);
                    }
                } catch (final LockException e) {
                    NativeStructuralIndex.LOG.warn("Failed to lock structural index: {}", e.getMessage(), e);
                // } catch (ReadOnlyException e) {
                //    NativeStructuralIndex.LOG.warn("Read-only error: " + e.getMessage(), e);
                } catch (final Exception e) {
                    NativeStructuralIndex.LOG.warn("Exception caught while writing to structural index: {}", e.getMessage(), e);
                }
            }
        } finally {
            pending.clear();
        }
    }

    private byte[] computeKey(final byte type, final QName qname, final int documentId, final NodeId nodeId) {
        final SymbolTable symbols = index.getBrokerPool().getSymbols();
        final short sym = symbols.getSymbol(qname.getLocalPart());
        final short nsSym = symbols.getNSSymbol(qname.getNamespaceURI());
        final byte[] data = new byte[9 + nodeId.size()];

        data[0] = type;
        ByteConversion.shortToByteH(sym, data, 1);
        ByteConversion.shortToByteH(nsSym, data, 3);
        ByteConversion.intToByteH(documentId, data, 5);
        nodeId.serialize(data, 9);
        return data;
    }

    private byte[] computeKey(final byte type, final QName qname, final int documentId) {
        final SymbolTable symbols = index.getBrokerPool().getSymbols();
        final short sym = symbols.getSymbol(qname.getLocalPart());
        final short nsSym = symbols.getNSSymbol(qname.getNamespaceURI());
        final byte[] data = new byte[9];

        data[0] = type;
        ByteConversion.shortToByteH(sym, data, 1);
        ByteConversion.shortToByteH(nsSym, data, 3);
        ByteConversion.intToByteH(documentId, data, 5);
        return data;
    }

    private static byte[] computeKey(final byte type, final int documentId) {
        final byte[] data = new byte[5];

        data[0] = type;
        ByteConversion.intToByteH(documentId, data, 1);
        return data;
    }
    
    private byte[] computeDocKey(final byte type, final int documentId, final QName qname) {
        final SymbolTable symbols = index.getBrokerPool().getSymbols();
        final short sym = symbols.getSymbol(qname.getLocalPart());
        final short nsSym = symbols.getNSSymbol(qname.getNamespaceURI());
        final byte[] data = new byte[10];

        data[0] = 2;
        ByteConversion.intToByteH(documentId, data, 1);
        data[5] = type;
        ByteConversion.shortToByteH(sym, data, 6);
        ByteConversion.shortToByteH(nsSym, data, 8);
        return data;
    }

    private static byte[] computeDocKey(final int documentId) {
        final byte[] data = new byte[5];

        data[0] = 2;
        ByteConversion.intToByteH(documentId, data, 1);
        return data;
    }

    private static long computeValue(final NodeProxy proxy) {
        // dirty hack: encode the extra number of bits needed for the node id into the
        // storage address. this way, everything fits into the long address and
        // we don't need to change the btree.
        final long address = proxy.getInternalAddress();
        final short nodeIdLen = (short)(proxy.getNodeId().units() % 8);
        return address | ((long)(nodeIdLen << 24) & 0xFF000000L);
    }

    private static int readDocId(final byte[] key) {
        return ByteConversion.byteToIntH(key, 5);
    }

    private NodeId readNodeId(final byte[] key, final long value) {
        // extra number of bits of the node id is encoded in the long address
        short bits = (short)((value >>> 24) & 0xFFL);
        if (bits == 0) {
            bits = 8;
        }
        // compute total number of bits for node id
        final int units = (key.length - 10) * 8 + bits;
        return index.getBrokerPool().getNodeFactory().createFromData(units, key, 9);
    }

    private QName readQName(final byte[] key) {
        final SymbolTable symbols = index.getBrokerPool().getSymbols();
        final byte type = key[5];
        final short sym = ByteConversion.byteToShortH(key, 6);
        final short nsSym = ByteConversion.byteToShortH(key, 8);
        return new QName(symbols.getName(sym), symbols.getNamespace(nsSym), type);
    }

    private class NativeStructuralStreamListener extends AbstractStreamListener {

        private NativeStructuralStreamListener() {
            //Nothing to do
        }

        @Override
        public void startElement(final Txn transaction, final ElementImpl element, final NodePath path) {
            super.startElement(transaction, element, path);
            if (mode == ReindexMode.STORE || mode == ReindexMode.REMOVE_SOME_NODES) {
                final NodeProxy proxy = new NodeProxy(element.getExpression(), document, element.getNodeId(), Node.ELEMENT_NODE, element.getInternalAddress());
                proxy.setIndexType(element.getIndexType());
                addNode(element.getQName(), proxy);
            }
        }

        @Override
        public void endElement(final Txn transaction, final ElementImpl element, final NodePath path) {
            super.endElement(transaction, element, path);
        }

        @Override
        public void attribute(final Txn transaction, final AttrImpl attrib, final NodePath path) {
            if (mode == ReindexMode.STORE || mode == ReindexMode.REMOVE_SOME_NODES) {
                final NodeProxy proxy = new NodeProxy(null, document, attrib.getNodeId(), Node.ATTRIBUTE_NODE,
                    attrib.getInternalAddress());
                proxy.setIndexType(attrib.getIndexType());
                addNode(attrib.getQName(), proxy);
            }
            super.attribute(transaction, attrib, path);
        }

        @Override
        public IndexWorker getWorker() {
            return NativeStructuralIndexWorker.this;
        }
    }
}
