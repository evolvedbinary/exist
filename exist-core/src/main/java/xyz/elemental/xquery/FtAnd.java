/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class FtAnd extends FtBinaryOp {

    public FtAnd(FTMatch left, FTMatch right) {
        super(left, right);
    }

    @Override
    public Query evaluateToQuery(Sequence contextSequence, Item contextItem) {
        var leftQuery = getLeft().evaluateToQuery(contextSequence, contextItem);
        var rightQuery = getRight().evaluateToQuery(contextSequence, contextItem);
        var builder = new BooleanQuery.Builder();
        builder.add(leftQuery, BooleanClause.Occur.MUST);
        builder.add(rightQuery, BooleanClause.Occur.MUST);
        return builder.build();
    }
}
