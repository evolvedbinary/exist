/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.util.Optional;

public class FtOr extends FtBinaryOp {

    public FtOr(FTMatch left, FTMatch right) {
        super(left, right);
    }

    @Override
    public Optional<Query> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException  {
        var leftQuery = getLeft().evaluateToQuery(contextSequence, contextItem);
        var rightQuery = getRight().evaluateToQuery(contextSequence, contextItem);
        var builder = new BooleanQuery.Builder();

        if(leftQuery.isPresent() && rightQuery.isPresent()) {
            builder.add(leftQuery.get(), BooleanClause.Occur.SHOULD);
            builder.add(rightQuery.get(), BooleanClause.Occur.SHOULD);
            return Optional.of(builder.build());
        } else if (leftQuery.isPresent()) {
            return leftQuery;
        } else if (rightQuery.isPresent()) {
            return rightQuery;
        }else {
            return Optional.empty();
        }
    }
}
