package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.exist.EXistException;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.test.ExistEmbeddedServer;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class XQueryUpdate3Test
{

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void updatingCompatibilityAnnotation() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query =
                "xquery version \"3.0\"\n;" +
                "module namespace t=\"http://exist-db.org/xquery/test/examples\";\n" +
                "declare updating function" +
                "   t:upsert($e as element(), \n" +
                "          $an as xs:QName, \n" +
                "          $av as xs:anyAtomicType) \n" +
                "   {\n" +
                "   let $ea := $e/attribute()[fn:node-name(.) = $an]\n" +
                "   return\n" +
                "     $ea\n" +
                "   };";

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
        }
    }

    @Test
    public void simpleAnnotation() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query =
                "xquery version \"3.0\"\n;" +
                        "module namespace t=\"http://exist-db.org/xquery/test/examples\";\n" +
                        "declare %simple function" +
                        "   t:upsert($e as element(), \n" +
                        "          $an as xs:QName, \n" +
                        "          $av as xs:anyAtomicType) \n" +
                        "   {\n" +
                        "   let $ea := $e/attribute()[fn:node-name(.) = $an]\n" +
                        "   return\n" +
                        "     $ea\n" +
                        "   };";

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
        }
    }

    @Test
    public void simpleAnnotationIsInvalidForVariableDeclaration() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query = "declare %simple variable $ab := 1;";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.prolog();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();
            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.prolog(ast, expr);
        }
        catch(XPathException ex) {
            assertEquals(ErrorCodes.XUST0032, ex.getErrorCode());
        }
    }

    @Test
    public void testingForUpdatingFunction() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query = "%simple function ( * )";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.sequenceType();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            SequenceType type = new SequenceType();
            treeParser.sequenceType(ast, type);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }
        }
    }

    @Test
    public void revalidationDeclaration() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query = "declare revalidation strict;";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.prolog();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.prolog(ast, expr);

            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }
        }
    }

    @Test
    public void transformWith() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query = "$e transform with { $e + 1 }\n";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.expr();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            Expression ret = treeParser.expr(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }

            assertTrue(ret instanceof CopyModifyExpression);
        }
    }

    @Test
    public void copyModifyExprTest() throws EXistException, RecognitionException, XPathException, TokenStreamException, PermissionDeniedException
    {
        String query = "copy $je := $e\n" +
                "   modify $je\n" +
                "   return $je";

        BrokerPool pool = BrokerPool.getInstance();
        try(final DBBroker broker = pool.getBroker()) {
            // parse the query into the internal syntax tree
            XQueryContext context = new XQueryContext(broker.getBrokerPool());
            XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
            XQueryParser xparser = new XQueryParser(lexer);
            xparser.expr();
            if (xparser.foundErrors()) {
                fail(xparser.getErrorMessage());
                return;
            }

            XQueryAST ast = (XQueryAST) xparser.getAST();

            XQueryTreeParser treeParser = new XQueryTreeParser(context);
            PathExpr expr = new PathExpr(context);
            treeParser.expr(ast, expr);
            if (treeParser.foundErrors()) {
                fail(treeParser.getErrorMessage());
                return;
            }
        }
    }
}
