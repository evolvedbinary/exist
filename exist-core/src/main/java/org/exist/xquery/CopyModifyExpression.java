package org.exist.xquery;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.util.ArrayList;
import java.util.List;

public class CopyModifyExpression extends PathExpr {

    public static class CopySource {
        protected String varName;
        protected Expression inputSequence;

        public String getVariable() {
            return this.varName;
        }
        public Expression getInputSequence() {
            return this.inputSequence;
        }

        public void setVariable(String varName) {
            this.varName = varName;
        }

        public void setInputSequence(Expression inputSequence) {
            this.inputSequence = inputSequence;
        }

        public CopySource(String name, Expression value) {
            this.varName = name;
            this.inputSequence = value;
        }

        public CopySource() {
        }
    }


    protected List<CopySource> sources;
    protected Expression modifyExpr;
    protected Expression returnExpr;

    // see https://www.w3.org/TR/xquery-update-30/#id-copy-modify for details
    public Category getCategory() {
        // placeholder implementation
        return Category.SIMPLE;
    }

    public CopyModifyExpression(XQueryContext context) {
        super(context);
        this.sources = new ArrayList<CopySource>();
    }

    public void addCopySource(String varName, Expression value) {
        this.sources.add(new CopySource(varName, value));
    }

    public void setModifyExpr(Expression expr) {
        this.modifyExpr = expr;
    }

    public Expression getModifyExpr() {
        return this.modifyExpr;
    }

    public void setReturnExpr(Expression expr) {
        this.returnExpr = expr;
    }

    public Expression getReturnExpr() {
        return this.returnExpr;
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
        dumper.display("copy").nl();
        dumper.startIndent();
        for(int i = 0; i < sources.size(); i++)
        {
            dumper.display("$").display(sources.get(i).varName);
            dumper.display(" := ");
            sources.get(i).inputSequence.dump(dumper);
        }
        dumper.endIndent();
        dumper.display("modify").nl();
        modifyExpr.dump(dumper);
        dumper.nl().display("return ");
        dumper.startIndent();
        returnExpr.dump(dumper);
        dumper.endIndent();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("copy ");
        for(int i = 0; i < sources.size(); i++)
        {
            result.append("$").append(sources.get(i).varName);
            result.append(sources.get(i).inputSequence.toString());
            if (sources.size() > 1 && i < sources.size() - 1)
                result.append(", ");
            else
                result.append(" ");
        }
        result.append(" ");
        result.append("modify ");
        result.append(modifyExpr.toString());
        result.append(" ");
        result.append("return ");
        result.append(returnExpr.toString());
        return result.toString();
    }
}
