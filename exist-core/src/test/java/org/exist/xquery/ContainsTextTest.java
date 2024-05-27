/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package org.exist.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.fail;

public class ContainsTextTest {

    @Test
    void parseFunction() throws TokenStreamException, XPathException, RecognitionException {
        String function =
                "module namespace blabla = \"http://www.example.com\";" +
                "declare namespace fn=\"http://www.w3.org/2005/xpath-functions\";\n" +
                "declare function blabla:fn-with-contains \n" +
                "  ( $param as xs:string? ,\n" +
                "    $secondParam as xs:string* )  as xs:boolean {\n" +
                "\n" +
                "   some $param in $secondParam\n" +
                "   satisfies fn:contains($param,$secondParam)\n" +
                " } ;";
        parse(function);

    }

    @Test
    void simpleContainsText() throws RecognitionException, XPathException, TokenStreamException, IOException {
        final String query = "xquery version \"3.1\";" +
                "for $w in ('aaaa', 'bbbb', 'ccccc')\n" +
                " where $w contains text \"aaaa\" " +
                "return <text>{ $w }</text>";

        parse(query);
    }

    private PathExpr parse(String query) throws TokenStreamException, XPathException, RecognitionException {
        // parse the query into the internal syntax tree
        final XQueryContext context = new XQueryContext();

        final XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
        final XQueryParser xparser = new XQueryParser(lexer);
        xparser.xpath();
        if (xparser.foundErrors()) {
            fail(xparser.getErrorMessage());
        }
        final XQueryAST ast = (XQueryAST) xparser.getAST();

        System.out.println("ast = " + ast.toStringList());

        final XQueryTreeParser treeParser = new XQueryTreeParser(context);
        final PathExpr expr = new PathExpr(context);
        treeParser.xpath(ast, expr);
        if (treeParser.foundErrors()) {
            fail(treeParser.getErrorMessage());
        }
        return  expr;
    }
}