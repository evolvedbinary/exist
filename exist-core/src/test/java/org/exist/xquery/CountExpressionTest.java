package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.exist.EXistException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.test.ExistEmbeddedServer;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class CountExpressionTest
{

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void countTest() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "for $p in $products\n" +
                "order by $p/sales descending\n" +
                "count $rank\n" +
                "where $rank <= 3\n" +
                "return\n" +
                "   <product rank=\"{$rank}\">\n" +
                "      {$p/name, $p/sales}\n" +
                "   </product>";

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

            // count keyword
            assertEquals(XQueryParser.LITERAL_count, ast.getNextSibling().getFirstChild().getNextSibling().getNextSibling().getType());
            // rank variable binding
            assertEquals(XQueryParser.VARIABLE_BINDING, ast.getNextSibling().getFirstChild().getNextSibling().getNextSibling().getFirstChild().getType());
        }
    }
}
