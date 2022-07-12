package org.exist.xquery;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.Type;

public class InsertExpr extends AbstractExpression
{
    public enum Choice {
        FIRST,
        LAST,
        INTO,
        AFTER,
        BEFORE
    }

    protected final Expression source;
    protected final Expression target;
    protected final Choice choice;

    public InsertExpr(XQueryContext context, Expression source, Expression target, Choice choice) {
        super(context);
        this.source = source;
        this.target = target;
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
    public int returnsType()
    {
        // placeholder implementation
        return Type.EMPTY;
    }

    public Category getCategory() {
        // placeholder implementation
        return Category.UPDATING;
    }

    @Override
    public Cardinality getCardinality() {
        return Cardinality.ONE_OR_MORE;
    }

    @Override
    public void dump(ExpressionDumper dumper) {
        dumper.display("insert").nl();
        dumper.startIndent();
        source.dump(dumper);
        dumper.endIndent();
        dumper.display(choice).nl();
        dumper.startIndent();
        target.dump(dumper);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("insert ");
        result.append(source.toString());
        result.append(" ");
        result.append(choice.toString());
        result.append(" ");
        result.append(target.toString());
        return result.toString();
    }
}
