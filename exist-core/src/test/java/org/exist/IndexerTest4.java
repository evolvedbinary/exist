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
package org.exist;

import org.exist.collections.Collection;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;
import org.exist.test.ExistEmbeddedServer;
import org.exist.util.LockException;
import org.exist.util.MimeType;
import org.exist.util.StringInputSource;
import org.exist.xmldb.XmldbURI;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Optional;

import static org.exist.collections.CollectionConfiguration.DEFAULT_COLLECTION_CONFIG_FILE_URI;

/**
 * Tests the Indexer's ability to process CDATA Sections.
 *
 * @author <a href="mailto:adam@evolvedbinary.com>Adam Retter</a>.
 */
public class IndexerTest4 {

    // New instance per test, as tests can cause DB corruption
    @Rule
    public final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    /**
     * It is possible to store a document into eXist-db containing an Element with two children:
     * <ol>
     *     <li>a Text node,</li>
     *     <li>followed by a CData Section.</li>
     * </ol>
     *
     * See: <a href="https://github.com/eXist-db/exist/issues/4825">Range Index error prevents storing Element containing CDATA that is preceeded by Text node, and corrupt db</a>
     */
    @Test
    public void elemWithTextAndCdataChildren() throws EXistException, PermissionDeniedException, IOException, SAXException, LockException {
        final BrokerPool brokerPool = existEmbeddedServer.getBrokerPool();

        final XmldbURI testCollectionUri = XmldbURI.create("/db/indexer-test-elem-text-cdata");
        final XmldbURI testDocumentUri = XmldbURI.create("test1.xml");

        final String testDocument = "<entry>something<![CDATA[Item]]></entry>";

        // Create Collection
        try (final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()));
             final Txn transaction = brokerPool.getTransactionManager().beginTransaction()) {

            try (final Collection testCollection = broker.getOrCreateCollection(transaction, testCollectionUri)) {
            }

            transaction.commit();
        }

        // Store the Document
        try (final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()));
             final Txn transaction = brokerPool.getTransactionManager().beginTransaction()) {

            try (final Collection testCollection = broker.getOrCreateCollection(transaction, testCollectionUri)) {
                testCollection.storeDocument(transaction, broker, testDocumentUri, new StringInputSource(testDocument), MimeType.XML_TYPE);
            }

            transaction.commit();
        }
    }

    /**
     * It was not possible to store a document into eXist-db if the following two concerns align:
     *
     * <ol>
     *     <li>The document contains an Element with two children:
     *          <ol>
     *              <li>a Text node,</li>
     *              <li>followed by a CData Section.</li>
     *          </ol>
     *     <li>
     *     <li>There is a Range Index configured on the Element for the Collection in which the document is to be stored.</li>
     * </ol>
     *
     * See: <a href="https://github.com/eXist-db/exist/issues/4825">Range Index error prevents storing Element containing CDATA that is preceeded by Text node, and corrupt db</a>
     */
    @Test
    public void elemWithTextAndCdataChildrenAndRangeIndex() throws EXistException, PermissionDeniedException, IOException, SAXException, LockException {
        final BrokerPool brokerPool = existEmbeddedServer.getBrokerPool();

        final String testCollectionConfig = """
                <collection xmlns="http://exist-db.org/collection-config/1.0">
                    <index>
                        <create qname="entry" type="xs:string"/>
                    </index>
                </collection>""";

        final XmldbURI testCollectionUri = XmldbURI.create("/db/indexer-test-rangeindex-elem-text-cdata");
        final XmldbURI testDocumentUri = XmldbURI.create("test1.xml");

        final String testDocument = "<entry>something<![CDATA[Item]]></entry>";

        // Create Collection, and store the Index config
        try (final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()));
             final Txn transaction = brokerPool.getTransactionManager().beginTransaction()) {

            try (final Collection testCollection = broker.getOrCreateCollection(transaction, testCollectionUri)) {
                try (final Collection testConfigCollection = broker.getOrCreateCollection(transaction, XmldbURI.CONFIG_COLLECTION_URI.append(testCollectionUri))) {
                    testConfigCollection.storeDocument(transaction, broker, DEFAULT_COLLECTION_CONFIG_FILE_URI, new StringInputSource(testCollectionConfig), MimeType.XML_TYPE);
                }
            }

            transaction.commit();
        }

        // Store the Document
        try (final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()));
             final Txn transaction = brokerPool.getTransactionManager().beginTransaction()) {

            try (final Collection testCollection = broker.getOrCreateCollection(transaction, testCollectionUri)) {
                testCollection.storeDocument(transaction, broker, testDocumentUri, new StringInputSource(testDocument), MimeType.XML_TYPE);
            }

            transaction.commit();
        }
    }
}
