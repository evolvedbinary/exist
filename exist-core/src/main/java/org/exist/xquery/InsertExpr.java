package org.exist.xquery;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class InsertExpr extends ModifyingExpression {
    public enum Choice {
        FIRST,
        LAST,
        INTO,
        AFTER,
        BEFORE
    }

    protected final Expression sourceExpr;
    protected final Choice choice;

    public InsertExpr(XQueryContext context, Expression source, Expression target, Choice choice) {
        super(context, target);
        this.sourceExpr = source;
        this.choice = choice;
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
        dumper.display("insert").nl();
        dumper.startIndent();
        sourceExpr.dump(dumper);
        dumper.endIndent();
        dumper.display(choice).nl();
        dumper.startIndent();
        targetExpr.dump(dumper);
        dumper.endIndent();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("insert ");
        result.append(sourceExpr.toString());
        result.append(" ");
        result.append(choice.toString());
        result.append(" ");
        result.append(targetExpr.toString());
        return result.toString();
    }

    public Choice getChoice() {
        return choice;
    }

    public Expression getSourceExpr() {
        return sourceExpr;
    }
}
