package org.exist.xquery;

import org.exist.xquery.value.Type;

public abstract class ModifyingExpression extends AbstractExpression {

    protected final Expression targetExpr;

    public ModifyingExpression(XQueryContext context, Expression target) {
        super(context);
        this.targetExpr = target;
    }

    @Override
    public int returnsType() {
        // placeholder implementation
        return Type.EMPTY;
    }

    public Category getCategory() {
        return Category.UPDATING;
    }

    public Expression getTargetExpr() {
        return targetExpr;
    }
}
