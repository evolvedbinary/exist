/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package org.exist.xquery;

import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class FTComparison extends BinaryOp {


    public FTComparison(XQueryContext context) {
        super(context);
    }

    public FTComparison(XQueryContext context, Expression left, Expression right) {
        super(context);
        setLeft(left);
        setRight(right);
    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {

        var left = getLeft().eval(contextSequence, contextItem);

        //Assume it's just string at the moment.
        var right = getRight().eval(contextSequence, contextItem);


        var leftString = left.getStringValue(); //TODO - this is not correct, we need to iterate over all items here.
                                                //       left.iterate()

        var rightString = right.getStringValue();

        return BooleanValue.valueOf(leftString.contains(rightString));
    }
}