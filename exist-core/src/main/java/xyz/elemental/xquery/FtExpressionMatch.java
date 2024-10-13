/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.*;
import org.exist.xquery.Expression;
import org.exist.xquery.PathExpr;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.util.ArrayList;
import java.util.Optional;


public class FtExpressionMatch extends FTMatch {

    final Expression expression;

    public FtExpressionMatch(PathExpr expression) {
        this.expression = expression;
    }

    @Override
    public Optional<Query> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException {
        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var gg = new ArrayList<Query>();

        for(var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringifies = iterator.nextItem().getStringValue();
            var maybeQuery = LuceneQueryProducer.stringToQuery(stringifies);
            maybeQuery.ifPresent(gg::add);
        }

        if(gg.isEmpty()) {
            return Optional.empty();
        }else if (gg.size() == 1) {
            return Optional.of(gg.get(0));
        }else {
            var builder = new BooleanQuery.Builder();
            gg.forEach(x -> builder.add(x, BooleanClause.Occur.SHOULD));
            return Optional.of(builder.build());
        }
    }

    @Override
    public String toString() {
        return "FTExpressionMatch [expression=" + expression + "]";
    }



}
