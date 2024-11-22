/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.exist.xquery.PathExpr;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.fail;

public class FtSearchParserTest {

    @Test
    void parseFunction() throws Exception {
        String function =
                "module namespace blabla = \"http://www.example.com\";" +
                        "declare namespace fn=\"http://www.w3.org/2005/xpath-functions\";\n" +
                        "declare function blabla:fn-with-contains \n" +
                        "  ( $param as xs:string? ,\n" +
                        "    $secondParam as xs:string* )  as xs:boolean {\n" +
                        "\n" +
                        "   some $param in $secondParam\n" +
                        "   satisfies contains($param,$secondParam)\n" +
                        " } ;";
        parse(function);

    }

    @Test
    void simpleContainsText() throws Exception {
        final String query = "xquery version \"3.1\";" +
                "for $w in ('aaaa', 'bbbb', 'ccccc')\n" +
                " where $w contains text \"aaaa\" " +
                "return <text>{ $w }</text>";

        parse(query);
    }

    @Test
    void parseScore() throws Exception {
        final var query = """
                xquery version "3.1";
                for $w score $s in ('aaaa', 'bbbb', 'ccccc')
                   where $w contains text "aaaa"
                  return <text>{ $w } { $s  }</text>
                """;
        parse(query);
    }

    @Test
    void parseFtOr() throws Exception {
        final var query = """
                xquery version "3.1";
                    for $w in ('aaaa', 'bbbb', 'ccccc')
                   where $w contains text "aaaa" ftor "cccc"
                  return <text>{ $w }</text>
                """;
        parse(query);
    }

    @Test
    void parseFtOrFtOr() throws Exception {
        final var query = """
                xquery version "3.1";
                for $w in ('aaaa', 'bbbb', 'ccccc')
                   where $w contains text "aaaa" ftor "bbbb" ftor "cccc"
                  return <text>{ $w }</text>
                """;
        parse(query);
    }


    @Test
    void parserAnyWord() throws Exception {
        final var query = """
                xquery version "3.1";
                for $w in ('aaaa', 'bbbb', 'ccccc')
                   where $w contains text {"aaaa", "bbbb", "cccc"} any word
                  return <text>{ $w }</text>
                """;
        parse(query);
    }

    @Test
    void parserExtendedSelection() throws Exception {
        final var query = """
                xquery version "3.1";
                declare namespace exq = "http://example.org/XQueryImplementation";
                for $w in ('aaaa', 'bbbb', 'ccccc')
                   where $w contains text (# exq:use-index #) {"aaaa" any word}
                  return <text>{ $w }</text>
                """;
        parse(query);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "$w contains text 'aaa' using no wildcards",
            "$w contains text 'aaa' using wildcards",
            "$w contains text 'aaa' using no stemming",
            "$w contains text 'aaa' using stemming",
            "$w contains text 'aaa' using diacritics insensitive",
            "$w contains text 'aaa' using diacritics sensitive",
            "$w contains text 'aaa' using option exq:aiOption 'smartAi'",
            "$w contains text ('usability' using stemming ftand 'testing' phrase) ftor ('users' using diacritics insensitive ftand 'testing' phrase)"
    })
    public void optionsParserTest(String where) throws Exception {
        final var queryHeader = """
                xquery version "3.1";
                declare namespace exq = "http://example.org/XQueryImplementation";
                for $w in ('aaa', 'bbbb', 'ccccc')
                 where \n""";

        final var query = queryHeader + where + " \nreturn <text>{ $w }</text>";
        parse(query);
    }


    /*
    declare ft-option  using case sensitive
;
declare ft-option  using stemming
 using case insensitive
;
declare variable $input-context external ;
     */
    @Test
    void parseProlog() throws Exception {
        final String query = "xquery version \"3.1\"; \n" +
                "declare ft-option using stemming ; \n" +
                "declare namespace exq = \"http://example.org/XQueryImplementation\"; \n" +
                //"declare ft-option ; \n" +
                "declare namespace fff = \"http://example.org/ffff\"; \n" +

                "for $w in ('aaaa', 'bbbb', 'ccccc')\n" +
                " where $w contains text \"aaaa\" " +
                "return <text>{ $w }</text>";

        parse(query);
    }


    private PathExpr parse(String query) throws Exception {
        // parse the query into the internal syntax tree
        final XQueryContext context = new XQueryContext();

        final XQueryLexer lexer = new XQueryLexer(context, new StringReader(query));
        final XQueryParser xparser = new XQueryParser(lexer);
        xparser.xpath();
        if (xparser.foundErrors()) {

            if(xparser.getLastException().getCause() != null) {
                xparser.getLastException().getCause().printStackTrace();
            }

            System.err.println("=========================");

            fail(xparser.getLastException());
        }
        final XQueryAST ast = (XQueryAST) xparser.getAST();

        //System.out.println("ast = " + ast.toStringList());

        final XQueryTreeParser treeParser = new XQueryTreeParser(context);
        final PathExpr expr = new PathExpr(context);
        treeParser.xpath(ast, expr);
        if (treeParser.foundErrors()) {
            if(treeParser.getLastException().getCause() != null) {
                treeParser.getLastException().getCause().printStackTrace();
            }

            System.err.println("=========================");

            fail(treeParser.getLastException());
        }
        return expr;
    }
}