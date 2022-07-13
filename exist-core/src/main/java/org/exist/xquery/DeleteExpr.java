package org.exist.xquery;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class DeleteExpr extends ModifyingExpression {

    public DeleteExpr(XQueryContext context, Expression target)
    {
        super(context, target);
    }

    @Override
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException
    {

    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException
    {
        return Sequence.EMPTY_SEQUENCE;
    }

    public void dump(ExpressionDumper dumper) {
        dumper.display("delete").nl();
        dumper.startIndent();
        targetExpr.dump(dumper);
        dumper.endIndent();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("delete ");
        result.append(" ");
        result.append(targetExpr.toString());
        return result.toString();
    }
}
