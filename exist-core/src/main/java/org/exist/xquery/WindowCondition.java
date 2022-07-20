package org.exist.xquery;

import com.ibm.icu.text.Collator;
import org.exist.dom.QName;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.util.ExpressionDumper;

public class WindowCondition
{
    @SuppressWarnings("unused")
    private final XQueryContext context;
    private Collator collator;
    private Expression whenExpression;
    private String posVar = null;
    private QName currentItem = null;
    private QName previousItem = null;
    private QName nextItem = null;
    private boolean only = false;

    public WindowCondition(XQueryContext context, Expression whenExpr,
                           QName current, QName previous, QName next, String posVar, boolean only) {
        this.whenExpression = whenExpr;
        this.context = context;
        this.currentItem = current;
        this.previousItem = previous;
        this.nextItem = next;
        this.posVar = posVar;
        this.only = only;
        this.collator = context.getDefaultCollator();
    }

    public void setCollator(String collation) throws XPathException {
        this.collator = context.getCollator(collation);
    }

    public Collator getCollator() {
        return this.collator;
    }

    @Override
    public String toString() {
        return this.only ? "only " : ""
                + "current " + this.currentItem  + " at " + this.posVar
                + " previous " + this.previousItem
                + " next " + this.nextItem
                + " when " + this.whenExpression.toString();
    }

    public QName getCurrentItem()
    {
        return currentItem;
    }

    public QName getNextItem()
    {
        return nextItem;
    }

    public QName getPreviousItem()
    {
        return previousItem;
    }

    public String getPosVar()
    {
        return posVar;
    }

    public boolean getOnly()
    {
        return only;
    }
}
