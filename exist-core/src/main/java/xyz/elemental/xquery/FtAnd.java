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

public class FtAnd extends FtBinaryOp {

    public FtAnd(FtSelection left, FtSelection right) {
        super(left, right);
    }

    @Override
    public Optional<FtQuery> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException {
        var leftQuery = getLeft().evaluateToQuery(contextSequence, contextItem);
        var rightQuery = getRight().evaluateToQuery(contextSequence, contextItem);
        var builder = new BooleanQuery.Builder();

        if (leftQuery.isPresent() && rightQuery.isPresent()) {
            builder.add(leftQuery.get().getLuceneQuery(), BooleanClause.Occur.MUST);
            builder.add(rightQuery.get().getLuceneQuery(), BooleanClause.Occur.MUST);
            var query = new FtQuery();
            query.setLuceneQuery(builder.build());
            query.getMatchOptions().addAll(leftQuery.get().getMatchOptions());
            query.getMatchOptions().addAll(rightQuery.get().getMatchOptions());

            return Optional.of(query);
        } else {
            return Optional.empty();
        }
    }
}
