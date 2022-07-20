package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.exist.EXistException;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.test.ExistEmbeddedServer;
import org.exist.util.LockException;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.junit.ClassRule;
import org.junit.Test;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class WindowClauseTest
{

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void simpleWindowConditions() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                        "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                        "    start at $s when fn:true()\n" +
                        "    only end at $e when $e - $s eq 2\n" +
                        "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
        }
    }

    @Test
    public void complexWindowCondition() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "   start $first next $second when $first/price < $second/price\n" +
                "   end $last next $beyond when $last/price > $beyond/price\n" +
                "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
        }
    }

    @Test
    public void noEndWindowCondition() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "   start $first next $second when $first/price < $second/price\n" +
                "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
        }
    }

    @Test
    public void slidingWindowClause() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for sliding window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when fn:true()\n" +
                "    only end at $e when $e - $s eq 2\n" +
                "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(WindowExpr.WindowType.SLIDING_WINDOW, ((WindowExpr) expr.getFirst()).getWindowType());
        }
    }

    @Test
    public void allWindowsVars() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "start $first at $s previous $start-previous next $start-next when fn:true()\n" +
                "only end $last at $e previous $end-previous next $end-next when $e - $s eq 2\n" +
                "return\n" +
                "  <window>{$first, $last}</window>\n";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals("first", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem().getStringValue());
            assertEquals("s", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals("start-previous", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem().getStringValue());
            assertEquals("start-next", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem().getStringValue());
            assertEquals("last", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getCurrentItem().getStringValue());
            assertEquals("e", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPosVar());
            assertEquals("end-previous", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPreviousItem().getStringValue());
            assertEquals("end-next", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getNextItem().getStringValue());
            assertEquals(true, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getOnly());
        }
    }

    @Test
    public void tumblingWindowAllWindowVarsNoOnly() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10)\n" +
                "    start $s at $spos previous $sprev next $snext when true() \n" +
                "    end $e at $epos previous $eprev next $enext when true()\n" +
                "return\n" +
                "  <window>{$first, $last}</window>\n";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(WindowExpr.WindowType.TUMBLING_WINDOW, ((WindowExpr) expr.getFirst()).getWindowType());
            assertEquals("s", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem().getStringValue());
            assertEquals("spos", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals("sprev", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem().getStringValue());
            assertEquals("snext", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem().getStringValue());
            assertEquals("e", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getCurrentItem().getStringValue());
            assertEquals("epos", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPosVar());
            assertEquals("eprev", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPreviousItem().getStringValue());
            assertEquals("enext", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getNextItem().getStringValue());
            assertEquals(false, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getOnly());
        }
    }

    @Test
    public void tumblingWindowAvgReturn() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when fn:true()\n" +
                "    only end at $e when $e - $s eq 2\n" +
                "return avg($w)";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem());
            assertEquals("s", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getCurrentItem());
            assertEquals("e", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getNextItem());
            assertEquals(true, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getOnly());
        }
    }

    @Test
    public void tumblingWindowNoEndWindowConditionPositional() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query = "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when $s mod 3 = 1\n" +
                "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try (final DBBroker broker = pool.getBroker())
        {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors())
            {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors())
            {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem());
            assertEquals("s", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition());
        }
    }

    @Test
    public void tumblingWindowNoEndWindowConditionCurrentItem() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query = "xquery version \"3.1\";\n" +
                "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start $first when $first mod 3 = 0\n" +
                "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try (final DBBroker broker = pool.getBroker())
        {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors())
            {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors())
            {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals("first", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem().getStringValue());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition());
        }
    }

    @Test
    public void slidingWindowAvgReturn() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query = "xquery version \"3.1\";\n" +
                "for sliding window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when fn:true()\n" +
                "    only end at $e when $e - $s eq 2\n" +
                "return avg($w)";

        BrokerPool pool = BrokerPool.getInstance();
        try (final DBBroker broker = pool.getBroker())
        {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors())
            {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors())
            {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(WindowExpr.WindowType.SLIDING_WINDOW, ((WindowExpr) expr.getFirst()).getWindowType());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem());
            assertEquals("s", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem());
            assertEquals("e", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getNextItem());
            assertEquals(true, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getOnly());
        }
    }

    @Test
    public void slidingWindowEndWithoutOnly() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query = "xquery version \"3.1\";\n" +
                "for sliding window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                "    start at $s when fn:true()\n" +
                "    end at $e when $e - $s eq 2\n" +
                "return <window>{ $w }</window>";

        BrokerPool pool = BrokerPool.getInstance();
        try (final DBBroker broker = pool.getBroker())
        {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors())
            {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors())
            {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(WindowExpr.WindowType.SLIDING_WINDOW, ((WindowExpr) expr.getFirst()).getWindowType());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem());
            assertEquals("s", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem());
            assertEquals("e", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPreviousItem());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getNextItem());
            assertEquals(false, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getOnly());
        }
    }

    @Test
    public void tumblingWindowRunUp() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query = "xquery version \"3.1\";\n" +
                "for tumbling window $w in $closings\n" +
                "   start $first next $second when $first/price < $second/price\n" +
                "   end $last next $beyond when $last/price > $beyond/price\n" +
                "return\n" +
                "   <run-up symbol=\"{$symbol}\">\n" +
                "      <start-date>{fn:data($first/date)}</start-date>\n" +
                "      <start-price>{fn:data($first/price)}</start-price>\n" +
                "      <end-date>{fn:data($last/date)}</end-date>\n" +
                "      <end-price>{fn:data($last/price)}</end-price>\n" +
                "   </run-up>";

        BrokerPool pool = BrokerPool.getInstance();
        try (final DBBroker broker = pool.getBroker())
        {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.xpath();
            if (xparser.foundErrors())
            {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.xpath(ast, expr);
            if (treeParser.foundErrors())
            {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue("Expression should be of type WindowExpr", expr.getFirst() instanceof WindowExpr);
            assertEquals(WindowExpr.WindowType.TUMBLING_WINDOW, ((WindowExpr) expr.getFirst()).getWindowType());
            assertEquals("first", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getCurrentItem().getStringValue());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getStartWindowCondition().getPreviousItem());
            assertEquals("second", ((WindowExpr) expr.getFirst()).getStartWindowCondition().getNextItem().getStringValue());
            assertEquals("last", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getCurrentItem().getStringValue());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPosVar());
            assertEquals(null, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getPreviousItem());
            assertEquals("beyond", ((WindowExpr) expr.getFirst()).getEndWindowCondition().getNextItem().getStringValue());
            assertEquals(false, ((WindowExpr) expr.getFirst()).getEndWindowCondition().getOnly());
        }
    }
}
