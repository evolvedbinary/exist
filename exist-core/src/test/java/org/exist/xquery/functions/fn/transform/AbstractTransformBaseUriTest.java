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
package org.exist.xquery.functions.fn.transform;

import com.evolvedbinary.j8fu.function.Function2E;
import com.evolvedbinary.j8fu.tuple.Tuple3;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.security.PermissionDeniedException;
import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.XQueryPool;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.Txn;
import org.exist.test.ExistEmbeddedServer;
import org.exist.util.LockException;
import org.exist.util.MimeType;
import org.exist.util.StringInputSource;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static com.evolvedbinary.j8fu.tuple.Tuple.Tuple;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:adam@evolvedbinary.com">Adam Retter</a>
 */
public abstract class AbstractTransformBaseUriTest {

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    private static XmldbURI TEST_COLLECTION_URI = XmldbURI.create("/db/transform-baseuri-test");

    private static XmldbURI BASE_URI_XSLT_URI = TEST_COLLECTION_URI.append("base-uri.xslt");

    private static XmldbURI INPUT_XML_URI = TEST_COLLECTION_URI.append("input.xml");

    private static String BASE_URI_XSLT =
            "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\" version=\"2.0\">\n" +
            "  <xsl:output indent=\"no\" omit-xml-declaration=\"yes\" encoding=\"UTF-8\"/>\n" +
            "  <xsl:template match=\"a\">\n" +
            "    <xsl:copy>\n" +
            "      <baseURI>\n" +
            "        <withoutContextItem><xsl:value-of select=\"base-uri()\"/></withoutContextItem>\n" +
            "        <documentContextItem><xsl:value-of select=\"base-uri(root(.))\"/></documentContextItem>\n" +
            "        <elementContextItem><xsl:value-of select=\"base-uri(.)\"/></elementContextItem>\n" +
            "      </baseURI>\n" +
            "    </xsl:copy>\n" +
            "  </xsl:template>\n" +
            "</xsl:stylesheet>";

    private static String INPUT_XML =
            "<a>b</a>";

    @BeforeClass
    public static void setup() throws PermissionDeniedException, IOException, SAXException, EXistException, LockException {
        final BrokerPool brokerPool = existEmbeddedServer.getBrokerPool();
        try (final Txn transaction = brokerPool.getTransactionManager().beginTransaction();
             final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()))) {

            try (final Collection testCollection = broker.getOrCreateCollection(transaction, TEST_COLLECTION_URI)) {
                store(broker, transaction, testCollection.getURI(),
                        Tuple(BASE_URI_XSLT_URI, BASE_URI_XSLT, MimeType.XSLT_TYPE),
                        Tuple(INPUT_XML_URI, INPUT_XML, MimeType.XML_TYPE)
                );
            }

            transaction.commit();
        }
    }

    /**
     * Generate the XQuery for transforming a source node from a stylesheet location.
     *
     * @param stylesheetLocation the location of the stylesheet.
     * @param sourceNode the source node (as a string).
     *
     * @return the xquery.
     */
    protected abstract String xqueryTransformFromLocation(final String stylesheetLocation, final String sourceNode);

    /**
     * Generate the XQuery for transforming a source node from a stylesheet node in-memory.
     *
     * @param stylesheetNode the stylesheet node (as a string).
     * @param sourceNode the source node (as a string).
     *
     * @return the xquery.
     */
    protected abstract String xqueryTransformFromInMemoryNode(final String stylesheetNode, final String sourceNode);

    /**
     * Generate the XQuery for transforming a source node from a stylesheet node stored in the database.
     *
     * @param stylesheetDbLocation the stylesheet location in the database.
     * @param sourceNode the source node (as a string).
     *
     * @return the xquery.
     */
    protected abstract String xqueryTransformFromStoredNode(final XmldbURI stylesheetDbLocation, final String sourceNode);

    @Test
    public void baseUriFromDbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = BASE_URI_XSLT_URI.getCollectionPath();
        final String xquery = xqueryTransformFromLocation(xsltLocation, INPUT_XML);
        javax.xml.transform.Source expected = Input.fromString("<a><baseURI><withoutContextItem>file:/private/tmp/input.xml</withoutContextItem><documentContextItem>file:/private/tmp/input.xml</documentContextItem><elementContextItem>file:/private/tmp/input.xml</elementContextItem></baseURI></a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void baseUriFromXmldbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = XmldbURI.create(XmldbURI.EMBEDDED_SHORT_URI_PREFIX).append(BASE_URI_XSLT_URI).toString();
        final String xquery = xqueryTransformFromLocation(xsltLocation, INPUT_XML);
        javax.xml.transform.Source expected = Input.fromString("<a><baseURI><withoutContextItem>file:/private/tmp/input.xml</withoutContextItem><documentContextItem>file:/private/tmp/input.xml</documentContextItem><elementContextItem>file:/private/tmp/input.xml</elementContextItem></baseURI></a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void baseUriFromInMemoryNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromInMemoryNode(BASE_URI_XSLT, INPUT_XML);
        javax.xml.transform.Source expected = Input.fromString("<a><baseURI><withoutContextItem>file:/private/tmp/input.xml</withoutContextItem><documentContextItem>file:/private/tmp/input.xml</documentContextItem><elementContextItem>file:/private/tmp/input.xml</elementContextItem></baseURI></a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void baseUriFromStoredNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromStoredNode(BASE_URI_XSLT_URI, INPUT_XML);
        javax.xml.transform.Source expected = Input.fromString("<a><baseURI><withoutContextItem>file:/private/tmp/input.xml</withoutContextItem><documentContextItem>file:/private/tmp/input.xml</documentContextItem><elementContextItem>file:/private/tmp/input.xml</elementContextItem></baseURI></a>").build();
        assertXQuery(xquery, expected);
    }

    private void assertXQuery(final String xquery, final javax.xml.transform.Source expected) throws EXistException, XPathException, PermissionDeniedException, IOException {
        final BrokerPool brokerPool = existEmbeddedServer.getBrokerPool();
        try (final Txn transaction = brokerPool.getTransactionManager().beginTransaction();
             final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()))) {

            final Diff diff = withCompiledQuery(broker, new StringSource(xquery), compiledXQuery -> {
                final Sequence result = executeQuery(broker, compiledXQuery);

                javax.xml.transform.Source actual = Input.fromNode(result.itemAt(0).toJavaObject(Node.class)).build();
                return DiffBuilder.compare(expected)
                        .withTest(actual)
                        .checkForSimilar()
                        .build();
            });

            transaction.commit();

            assertFalse(diff.toString(), diff.hasDifferences());
        }
    }

    private Sequence executeQuery(final DBBroker broker, final CompiledXQuery compiledXQuery) throws PermissionDeniedException, XPathException {
        final BrokerPool pool = broker.getBrokerPool();
        final XQuery xqueryService = pool.getXQueryService();
        return xqueryService.execute(broker, compiledXQuery, null, new Properties());
    }

    private <T> T withCompiledQuery(final DBBroker broker, final Source source, final Function2E<CompiledXQuery, T, XPathException, PermissionDeniedException> op) throws XPathException, PermissionDeniedException, IOException {
        final BrokerPool pool = broker.getBrokerPool();
        final XQuery xqueryService = pool.getXQueryService();
        final XQueryPool xqueryPool = pool.getXQueryPool();
        final CompiledXQuery compiledQuery = compileQuery(broker, xqueryService, xqueryPool, source);
        try {
            return op.apply(compiledQuery);
        } finally {
            if (compiledQuery != null) {
                xqueryPool.returnCompiledXQuery(source, compiledQuery);
            }
        }
    }

    private CompiledXQuery compileQuery(final DBBroker broker, final XQuery xqueryService, final XQueryPool xqueryPool, final Source query) throws PermissionDeniedException, XPathException, IOException {
        CompiledXQuery compiled = xqueryPool.borrowCompiledXQuery(broker, query);
        XQueryContext context;
        if (compiled == null) {
            context = new XQueryContext(broker.getBrokerPool());
        } else {
            context = compiled.getContext();
            context.prepareForReuse();
        }

        if (compiled == null) {
            compiled = xqueryService.compile(context, query);
        } else {
            compiled.getContext().updateContext(context);
            context.getWatchDog().reset();
        }

        return compiled;
    }

    private static void store(final DBBroker broker, final Txn transaction, final XmldbURI collectionUri, final Tuple3<XmldbURI, String, MimeType>... xmls) throws PermissionDeniedException, IOException, SAXException, LockException, EXistException {
        try (final Collection collection = broker.openCollection(collectionUri, Lock.LockMode.WRITE_LOCK)) {
            for (final Tuple3<XmldbURI, String, MimeType> xml : xmls) {
                broker.storeDocument(transaction, xml._1.lastSegment(), new StringInputSource(xml._2.getBytes(UTF_8)), xml._3, collection);
            }
        }
    }
}
