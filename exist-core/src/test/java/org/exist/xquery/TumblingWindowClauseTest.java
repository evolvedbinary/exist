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
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.exist.xquery.value.Sequence;
import org.junit.ClassRule;
import org.junit.Test;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class TumblingWindowClauseTest {

    @ClassRule
    public static final ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(true, true);

    @Test
    public void query() throws EXistException, PermissionDeniedException, IOException, SAXException, LockException, RecognitionException, XPathException, TokenStreamException
    {
        String query =  "xquery version \"3.0\";\n" +
                        "for tumbling window $w in (2, 4, 6, 8, 10, 12, 14)\n" +
                        "    start at $s when fn:true()\n" +
                        "    only end at $e when $e - $s eq 2\n" +
                        "return <window>{ $w }</window>";

        // parse the query into the internal syntax tree
        XQueryLexer lexer = new XQueryLexer(new StringReader(query));
        XQueryParser xparser = new XQueryParser(lexer);
        xparser.xpath();
        if (xparser.foundErrors()) {
            fail(xparser.getErrorMessage());
            return;
        }

        AST ast = xparser.getAST();
    }
}
