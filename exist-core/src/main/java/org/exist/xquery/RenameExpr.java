package org.exist.xquery;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class RenameExpr extends ModifyingExpression {
    protected final Expression newNameExpr;

    public RenameExpr(XQueryContext context, Expression target, Expression newName) {
        super(context, target);
        this.newNameExpr = newName;
    }

    @Override
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        return Sequence.EMPTY_SEQUENCE;
    }

    @Override
    public Cardinality getCardinality() {
        return Cardinality.ONE_OR_MORE;
    }

    @Override
    public void dump(ExpressionDumper dumper) {
        dumper.display("replace").nl();
        dumper.startIndent();
        targetExpr.dump(dumper);
        dumper.endIndent();
        dumper.startIndent();
        newNameExpr.dump(dumper);
        dumper.endIndent();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("replace ");
        result.append(targetExpr.toString());
        result.append(" ");
        result.append(newNameExpr.toString());
        return result.toString();
    }

    public Expression getNewNameExpr() {
        return newNameExpr;
    }
}
