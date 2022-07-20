
package org.exist.xquery;

import org.exist.dom.persistent.*;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.Type;

/**
 * Implements a count clause inside a FLWOR expressions.
 *
 * @author
 */
public class CountClause extends AbstractFLWORClause {

    protected String varName;

    public CountClause(XQueryContext context, String countName) {
        super(context);
        this.varName = countName;
    }

    @Override
    public ClauseType getType() {
        return ClauseType.COUNT;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException
    {

    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException
    {
        return null;
    }

    @Override
    public void dump(ExpressionDumper dumper) {
        dumper.display("count", this.getLine());
        dumper.startIndent();
        dumper.display(this.varName);
        dumper.endIndent().nl();
    }
}