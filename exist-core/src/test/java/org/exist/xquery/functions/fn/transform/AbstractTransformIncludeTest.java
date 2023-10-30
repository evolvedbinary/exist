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
import com.evolvedbinary.j8fu.tuple.Tuple2;
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
public abstract class AbstractTransformIncludeTest {

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    private static XmldbURI TEST_COLLECTION_URI = XmldbURI.create("/db/transform-include-test");
    private static XmldbURI INCLUDING_XSLT_URI = TEST_COLLECTION_URI.append("including.xslt");
    private static XmldbURI IMPORTING_XSLT_URI = TEST_COLLECTION_URI.append("importing.xslt");
    private static XmldbURI TRANSFORMING_XSLT_URI = TEST_COLLECTION_URI.append("transforming.xslt");
    private static XmldbURI INCLUDED_XSLT_URI = TEST_COLLECTION_URI.append("included.xslt");

    private static String INCLUDING_XSLT =
            "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n" +
            "    <xsl:output omit-xml-declaration='yes'/>\n" +
            "    <xsl:include href='" + INCLUDED_XSLT_URI.lastSegmentString() + "'/>\n" +
            "    <xsl:template match='a'>\n" +
            "        <copied-a>\n" +
            "            <xsl:copy>\n" +
            "                <xsl:apply-templates/>\n" +
            "            </xsl:copy>\n" +
            "        </copied-a>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";

    private static String IMPORTING_XSLT =
            "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n" +
            "    <xsl:output omit-xml-declaration='yes'/>\n" +
            "    <xsl:import href='" + INCLUDED_XSLT_URI.lastSegmentString() + "'/>\n" +
            "    <xsl:template match='a'>\n" +
            "        <copied-a>\n" +
            "            <xsl:copy>\n" +
            "                <xsl:apply-templates/>\n" +
            "            </xsl:copy>\n" +
            "        </copied-a>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";

    private static String TRANSFORMING_XSLT =
            "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n" +
            "    <xsl:output omit-xml-declaration='yes'/>\n" +
            "    <xsl:template match='a'>\n" +
            "        <copied-a>\n" +
            "            <xsl:copy>\n" +
            "                <xsl:sequence select=\"transform(map{\n" +
            "                         'stylesheet-location': '" + INCLUDED_XSLT_URI + "',\n" +
            "                         'source-node': .\n" +
            "                         })?output\"/>\n" +
            "            </xsl:copy>\n" +
            "        </copied-a>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";

    private static String INCLUDED_XSLT =
            "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n" +
            "    <xsl:template match='b'>\n" +
            "        <copied-b>\n" +
            "            <xsl:copy>\n" +
            "                <xsl:apply-templates/>\n" +
            "            </xsl:copy>\n" +
            "        </copied-b>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";

    @BeforeClass
    public static void setup() throws PermissionDeniedException, IOException, SAXException, EXistException, LockException {
        final BrokerPool brokerPool = existEmbeddedServer.getBrokerPool();
        try (final Txn transaction = brokerPool.getTransactionManager().beginTransaction();
             final DBBroker broker = brokerPool.get(Optional.of(brokerPool.getSecurityManager().getSystemSubject()))) {

            try (final Collection testCollection = broker.getOrCreateCollection(transaction, TEST_COLLECTION_URI)) {
                storeXslt(broker, transaction, testCollection.getURI(),
                        Tuple(INCLUDING_XSLT_URI, INCLUDING_XSLT),
                        Tuple(IMPORTING_XSLT_URI, IMPORTING_XSLT),
                        Tuple(TRANSFORMING_XSLT_URI, TRANSFORMING_XSLT),
                        Tuple(INCLUDED_XSLT_URI, INCLUDED_XSLT)
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
    public void relativeXsltIncludeFromDbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = INCLUDING_XSLT_URI.getCollectionPath();
        final String xquery = xqueryTransformFromLocation(xsltLocation, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltIncludeFromXmldbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = XmldbURI.create(XmldbURI.EMBEDDED_SHORT_URI_PREFIX).append(INCLUDING_XSLT_URI).toString();
        final String xquery = xqueryTransformFromLocation(xsltLocation, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltIncludeFromInMemoryNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromInMemoryNode(INCLUDING_XSLT, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltIncludeFromStoredNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromStoredNode(INCLUDING_XSLT_URI, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltImportFromDbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = IMPORTING_XSLT_URI.getCollectionPath();
        final String xquery = xqueryTransformFromLocation(xsltLocation, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltImportFromXmldbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = XmldbURI.create(XmldbURI.EMBEDDED_SHORT_URI_PREFIX).append(IMPORTING_XSLT_URI).toString();
        final String xquery = xqueryTransformFromLocation(xsltLocation, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltImportFromInMemoryNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromInMemoryNode(IMPORTING_XSLT, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltImportFromStoredNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromStoredNode(IMPORTING_XSLT_URI, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltTransformingFromDbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = TRANSFORMING_XSLT_URI.getCollectionPath();
        final String xquery = xqueryTransformFromLocation(xsltLocation, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltTransformingFromXmldbUri() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xsltLocation = XmldbURI.create(XmldbURI.EMBEDDED_SHORT_URI_PREFIX).append(TRANSFORMING_XSLT_URI).toString();
        final String xquery = xqueryTransformFromLocation(xsltLocation, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltTransformingFromInMemoryNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromInMemoryNode(TRANSFORMING_XSLT, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
        assertXQuery(xquery, expected);
    }

    @Test
    public void relativeXsltTransformingFromStoredNode() throws XPathException, PermissionDeniedException, EXistException, IOException {
        final String xquery = xqueryTransformFromStoredNode(TRANSFORMING_XSLT_URI, "<a>AA<b>BB</b></a>");
        javax.xml.transform.Source expected = Input.fromString("<copied-a><a>AA<copied-b><b>BB</b></copied-b></a></copied-a>").build();
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

    private static void storeXslt(final DBBroker broker, final Txn transaction, final XmldbURI collectionUri, final Tuple2<XmldbURI, String>... xslts) throws PermissionDeniedException, IOException, SAXException, LockException, EXistException {
        try (final Collection collection = broker.openCollection(collectionUri, Lock.LockMode.WRITE_LOCK)) {
            for (final Tuple2<XmldbURI, String> xslt : xslts) {
                broker.storeDocument(transaction, xslt._1.lastSegment(), new StringInputSource(xslt._2.getBytes(UTF_8)), MimeType.XSLT_TYPE, collection);
            }
        }
    }
}
