/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.xquery;

import org.exist.dom.QName;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements an XQuery let-expression.
 *
 * @author <a href="mailto:wolfgang@exist-db.org">Wolfgang Meier</a>
 */
public class WindowExpr extends BindingExpression {

    public enum WindowType {
        TUMBLING_WINDOW,
        SLIDING_WINDOW
    }

    //private Expression inputSequence = null;
    private WindowCondition startWindowCondition = null;
    private WindowCondition endWindowCondition = null;

    private WindowType windowType = WindowType.TUMBLING_WINDOW;

    public WindowExpr(XQueryContext context, WindowType type, WindowCondition start, WindowCondition end) {
        super(context);
        //this.inputSequence = inputSequence;
        this.windowType = type;
        this.startWindowCondition = start;
        this.endWindowCondition = end;
    }

    @Override
    public ClauseType getType() {
        return ClauseType.WINDOW;
    }

    public WindowType getWindowType() { return this.windowType; }

    public WindowCondition getStartWindowCondition()
    {
        return startWindowCondition;
    }

    public WindowCondition getEndWindowCondition()
    {
        return endWindowCondition;
    }

    @Override
    public void analyze(final AnalyzeContextInfo contextInfo) throws XPathException {
        super.analyze(contextInfo);
        // TODO
    }

    public Sequence eval(Sequence contextSequence, Item contextItem)
            throws XPathException {
        // TODO

        Sequence resultSequence = null;
        return resultSequence;
    }

    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
        dumper.display(this.getWindowType() == WindowType.TUMBLING_WINDOW ? "tumbling window " : "sliding window ", line);
        dumper.startIndent();
        dumper.display("$").display(varName);
        if (sequenceType != null) {
            dumper.display(" as ").display(sequenceType);
        }
        dumper.display(" in ");
        inputSequence.dump(dumper);
        dumper.endIndent().nl();
        //TODO : QuantifiedExpr
        if (returnExpr instanceof LetExpr)
        {dumper.display(" ", returnExpr.getLine());}
        else
        {dumper.display("return", returnExpr.getLine());}
        dumper.startIndent();
        returnExpr.dump(dumper);
        dumper.endIndent().nl();
    }

    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(this.getWindowType() == WindowType.TUMBLING_WINDOW ? "tumbling window " : "sliding window ");
        result.append("$").append(varName);
        if (sequenceType != null)
        {result.append(" as ").append(sequenceType);}
        result.append(" in ");
        result.append(inputSequence.toString());
        result.append(" ");
        result.append("start " + startWindowCondition.toString());
        result.append(" end " + endWindowCondition.toString());
        //TODO : QuantifiedExpr
        if (returnExpr instanceof LetExpr)
        {result.append(" ");}
        else
        {result.append("return ");}
        result.append(returnExpr.toString());
        return result.toString();
    }

    public void accept(ExpressionVisitor visitor) {
        visitor.visitWindowExpression(this);
    }

    @Override
    public boolean allowMixedNodesInReturn() {
        return true;
    }
}