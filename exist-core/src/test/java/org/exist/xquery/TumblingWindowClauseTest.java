package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
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
import org.exist.xquery.value.Sequence;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.Assert.*;

public class TumblingWindowClauseTest {

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void simpleWindowConditions() throws EXistException, PermissionDeniedException, IOException, SAXException, LockException, RecognitionException, XPathException, TokenStreamException
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
    public void complexWindowCondition() throws EXistException, PermissionDeniedException, IOException, SAXException, LockException, RecognitionException, XPathException, TokenStreamException
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
    public void noEndWindowCondition() throws EXistException, PermissionDeniedException, IOException, SAXException, LockException, RecognitionException, XPathException, TokenStreamException
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
}
