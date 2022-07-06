package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.exist.EXistException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.test.ExistEmbeddedServer;
import org.exist.xquery.parser.*;
import org.junit.ClassRule;
import org.junit.Test;
import java.io.StringReader;
import static org.junit.Assert.*;

public class ReservedNamesConflictTest
{
    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void reservedNamesIssueTest() throws EXistException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.1\";\n" +
                "<foo copy-namespaces=\"bar\"/>,\n" +
                "<foo empty-sequence=\"bar\"/>,\n" +
                "<foo schema-element=\"bar\"/>";

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


           XQueryAST one = (XQueryAST) ast.getNextSibling().getFirstChild().getFirstChild();
           XQueryAST two = (XQueryAST) ast.getNextSibling().getFirstChild().getNextSibling().getFirstChild();
           XQueryAST three = (XQueryAST) ast.getNextSibling().getFirstChild().getNextSibling().getNextSibling().getFirstChild();

           assertEquals("copy-namespaces", one.getText());
           assertEquals("empty-sequence", two.getText());
           assertEquals("schema-element", three.getText());
           assertEquals(XQueryTokenTypes.ATTRIBUTE, one.getType());
           assertEquals(XQueryTokenTypes.ATTRIBUTE, two.getType());
           assertEquals(XQueryTokenTypes.ATTRIBUTE, three.getType());
        }
    }
}

