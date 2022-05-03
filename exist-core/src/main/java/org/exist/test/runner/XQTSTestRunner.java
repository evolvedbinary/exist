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

package org.exist.test.runner;

import com.evolvedbinary.j8fu.tuple.Tuple2;
import org.exist.EXistException;
import org.exist.dom.QName;
import org.exist.security.PermissionDeniedException;
import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.storage.BrokerPool;
import org.exist.util.DatabaseConfigurationException;
import org.exist.xquery.XPathErrorProvider;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * A JUnit test runner which can run the XML formatted XQTS tests
 * within eXist-DB.
 *
 * @author Alan Paxton
 */
public class XQTSTestRunner extends AbstractTestRunner {

    private final Document doc;
    private final XQTSTestSuite xqtsTestSuite;

    XQTSTestRunner(final Path path, final Document doc, final XQTSTestSuite xqtsTestSuite, final boolean parallel) {
        super(path, parallel);
        this.doc = doc;
        this.xqtsTestSuite = xqtsTestSuite;
    }

    private String getSuiteName() {
        return "xqts." + xqtsTestSuite.description;
    }

    @Override
    public Description getDescription() {
        final String suiteName = getSuiteName();
        final Description description = Description.createSuiteDescription(suiteName, EMPTY_ANNOTATIONS);
        for (final XQTSTest XQTSTest : xqtsTestSuite.XQTSTests) {
            description.addChild(Description.createTestDescription(suiteName, XQTSTest.name + ":" + XQTSTest.description));
        }
        return description;
    }

    /**
     * Implement the method to run the suite that is the core of a JUnit runner
     *
     * @param notifier tell JUnit whenever things happen with test runs
     */
    @Override
    public void run(final RunNotifier notifier) {
        System.out.println("running the tests from XQTSTestRunner: " + xqtsTestSuite.description);
        try {
            for (final XQTSTest XQTSTest : xqtsTestSuite.XQTSTests) {
                final Description description = Description.createTestDescription(getSuiteName(), XQTSTest.name + ":" + XQTSTest.description);

                notifier.fireTestStarted(description);
                try {
                    final Sequence result = executeOperation(XQTSTest);
                    if (XQTSTest.assertEq != null) {
                        final Failure failure = new Failure(description, new XQTSAssertionFailure(XQTSTest.assertEq, result));
                        if (!result.hasOne()) {
                            notifier.fireTestFailure(failure);
                        } else if (!XQTSTest.assertEq.equals(result.itemAt(0).getStringValue())) {
                            notifier.fireTestFailure(failure);
                        }
                    }
                } catch (final XQTSTestException xqtsTestException) {
                    final Optional<QName> expected = XQTSTest.errorCode.map(errorCode -> new QName(errorCode, "http://www.w3.org/2005/xqt-errors", "err"));
                    if (!Optional.of(xqtsTestException.qName).equals(expected)) {
                        notifier.fireTestFailure(new Failure(description, xqtsTestException));
                    }
                } finally {
                    notifier.fireTestFinished(description);
                }

            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute the operation in the xTest, within eXist.
     * The core step of running the test(s).
     * Relies on {@link AbstractTestRunner} and {@link XSuite} to have the embedded server class usable
     *
     * @param XQTSTest the test containing an operation
     */
    private Sequence executeOperation(final XQTSTest XQTSTest) throws XQTSTestException {

        final Source query = new StringSource(XQTSTest.operation);
        final List<java.util.function.Function<XQueryContext, Tuple2<String, Object>>> externalVariableDeclarations = Arrays.asList(context -> new Tuple2<>("doc", doc), context -> new Tuple2<>("id", Sequence.EMPTY_SEQUENCE));


        // NOTE: at this stage EXIST_EMBEDDED_SERVER_CLASS_INSTANCE in XSuite will be usable
        final BrokerPool brokerPool = XSuite.EXIST_EMBEDDED_SERVER_CLASS_INSTANCE.getBrokerPool();
        try {
            return executeQuery(brokerPool, query, externalVariableDeclarations);
        } catch (final XPathException e) {
            e.printStackTrace();
            throw new XQTSTestException(e.getMessage(), e);
        } catch (final EXistException | PermissionDeniedException | IOException | DatabaseConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The parsed description of the XQTS tests
     * This is not comprehensive; it has been built to be enough to run the fn:format-integer tests
     */
    static class XQTSTestSuite {
        final String description;
        final List<XQTSTest> XQTSTests;

        public XQTSTestSuite(final String description, final List<XQTSTest> XQTSTests) {
            this.description = description;
            this.XQTSTests = XQTSTests;
        }
    }

    /**
     * Descriptor of the test, built from the test node in the document
     */
    private static class XQTSTest {
        String suite;
        String name;
        String description;
        String operation;
        String assertEq;
        Optional<String> errorCode = Optional.empty();
        Optional<String> language = Optional.empty();

        static XQTSTest fromXNode(final XQTSNode xnode) throws XQTSTestException {
            final XQTSTest xTest = new XQTSTest();
            xTest.operation = xnode.text("test");
            xTest.name = xnode.attr("name").orElse("<unknown name>");
            xTest.description = xnode.text("description");

            final Optional<XQTSNode> assertEq = xnode.first(Arrays.asList("result", "assert-eq"));
            assertEq.ifPresent(xNode -> xTest.assertEq = xNode.textContent().replaceAll("^'|'$", ""));

            final Optional<XQTSNode> error = xnode.first(Arrays.asList("result", "error"));
            error.ifPresent(xNode -> xTest.errorCode = xNode.attr("code"));

            for (final XQTSNode dependency : xnode.all("dependency")) {
                final Optional<String> type = dependency.attr("type");
                final Optional<String> value = dependency.attr("value");
                if (type.isPresent() && value.isPresent()) {
                    xTest.language = value;
                }
            }

            return xTest;
        }


    }

    /**
     * Quick wrapper shorthand around picking contents out of nodes
     */
    private static class XQTSNode {

        private final Node node;

        XQTSNode(final Node node) {
            this.node = node;
        }

        private List<XQTSNode> all(final List<String> path) {

            List<XQTSNode> level = new ArrayList<>();
            level.add(this);

            for (final String step : path) {
                final List<XQTSNode> nextLevel = new ArrayList<>();
                for (final XQTSNode jNode : level) {
                    final NodeList children = jNode.node.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        final Node child = children.item(i);
                        if (step.equals(child.getNodeName())) {
                            nextLevel.add(new XQTSNode(child));
                        }
                    }
                }
                level = nextLevel;
            }
            return level;
        }

        @SuppressWarnings("SameParameterValue")
        private List<XQTSNode> all(final String step) {
            return all(Collections.singletonList(step));
        }

        private Optional<XQTSNode> first(final List<String> path) {
            final List<XQTSNode> all = this.all(path);
            if (all.size() > 0) {
                return Optional.of(all.get(0));
            } else {
                return Optional.empty();
            }
        }

        private Optional<XQTSNode> first(final String path) {
            return first(Collections.singletonList(path));
        }

        private String text(final String label) {
            return first(label).map(xNode -> xNode.node.getTextContent()).orElse("");
        }

        private Optional<String> attr(final String label) {
            return Optional.ofNullable(node.getAttributes().getNamedItem(label)).map(Node::getNodeValue);
        }

        private String textContent() {
            return node.getTextContent();
        }
    }

    /**
     * Build test descriptions / suite from a document
     *
     * @param document the document describing the tests
     * @param suite    a descriptive name of the test suite
     * @return a suite containing a list of tests
     */
    public static XQTSTestSuite getTestsFromDocument(final Document document, final String suite) throws XQTSTestException {

        final XQTSNode root = new XQTSNode(document);
        final List<XQTSNode> testCases = root.all(Arrays.asList("test-set", "test-case"));
        final String[] description = new String[1];
        description[0] = "";
        root.first("test-set").ifPresent(xNode -> description[0] = xNode.text("description"));
        final String suiteDescription = description[0].length() == 0 ? suite : description[0];
        final List<XQTSTest> tests = new ArrayList<>();
        for (final XQTSNode testCase : testCases) {
            final XQTSTest xTest = XQTSTest.fromXNode(testCase);
            xTest.suite = suiteDescription;
            tests.add(xTest);
        }
        return new XQTSTestSuite(suiteDescription, tests);
    }

    /**
     * Exception passed as {@link RunNotifier} failure wrapping an exception is thrown in the xQuery execution
     */
    static class XQTSTestException extends Exception {

        final QName qName;

        XQTSTestException(final String message, final XPathErrorProvider errorProvider) {
            super(message);
            qName = errorProvider.getErrorCode().getErrorQName();
        }
    }

    /**
     * Exception passed as {@link RunNotifier} failure when an assertion from the XQTS test fails
     * against the result of an xQuery execution.
     */
    static class XQTSAssertionFailure extends Exception {

        final String expected;
        final Sequence actual;

        XQTSAssertionFailure(final String expected, final Sequence actual) {
            super("Expected " + expected + " but was " + actual);
            this.expected = expected;
            this.actual = actual;
        }
    }
}
