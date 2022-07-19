package org.exist.xquery;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class ReplaceExpr extends ModifyingExpression{
    public enum ReplacementType {
        NODE,
        VALUE
    }

    protected final Expression withExpr;
    protected final ReplaceExpr.ReplacementType replacementType;

    public ReplaceExpr(XQueryContext context, Expression target, Expression with, ReplaceExpr.ReplacementType replacementType) {
        super(context, target);
        this.withExpr = with;
        this.replacementType = replacementType;
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
        dumper.display(replacementType).nl();
        dumper.startIndent();
        withExpr.dump(dumper);
        dumper.endIndent();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("replace ");
        result.append(targetExpr.toString());
        result.append(" ");
        result.append(replacementType.toString());
        result.append(" ");
        result.append(withExpr.toString());
        return result.toString();
    }

    public ReplaceExpr.ReplacementType getReplacementType() {
        return replacementType;
    }

    public Expression getWithExpr() {
        return withExpr;
    }
}
