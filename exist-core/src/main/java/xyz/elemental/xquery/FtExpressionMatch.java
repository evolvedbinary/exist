/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.exist.xquery.Expression;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;


public class FtExpressionMatch extends FtSelection {

    final Expression expression;
    final Optional<AnyAllOptions> anyAllOptions;

    public FtExpressionMatch(Expression expression, AnyAllOptions anyAllOptions) {
        this.expression = expression;
        this.anyAllOptions = Optional.ofNullable(anyAllOptions);
    }

    @Override
    public Optional<Query> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException {
        return switch (anyAllOptions.orElse(AnyAllOptions.ANY)) {
            case ANY -> evaluateToQueryAnyAll(contextSequence, contextItem, false);
            case ALL -> evaluateToQueryAnyAll(contextSequence, contextItem, true);
            case PHRASE -> evaluateToQueryPhrase(contextSequence, contextItem);
            case ANY_WORD -> evaluateToQueryAnyAllWords(contextSequence, contextItem, BooleanClause.Occur.SHOULD);
            case ALL_WORDS -> evaluateToQueryAnyAllWords(contextSequence, contextItem, BooleanClause.Occur.MUST);
        };
    }

    public Optional<Query> evaluateToQueryAnyAllWords(Sequence contextSequence, Item contextItem, BooleanClause.Occur occur) throws XPathException {

        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var tokens = new HashSet<String>();

        for (var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringValue = iterator.nextItem().getStringValue();
            tokens.addAll(LuceneQueryProducer.tokenize(stringValue));
        }

        if (tokens.isEmpty()) {
            return Optional.empty();
        } else if (tokens.size() == 1) {
            return Optional.of(new TermQuery(new Term(FIELD_NAME, tokens.stream().findFirst().get())));
        } else {
            var builder = new BooleanQuery.Builder();
            tokens.stream().forEach(token -> {
                builder.add(new TermQuery(new Term(FIELD_NAME, token)), occur);
            });
            return Optional.of(builder.build());
        }

    }

    public Optional<Query> evaluateToQueryPhrase(Sequence contextSequence, Item contextItem) throws XPathException {
        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var tokens = new ArrayList<String>();

        for (var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringValue = iterator.nextItem().getStringValue();
            tokens.addAll(LuceneQueryProducer.tokenize(stringValue));
        }

        if (tokens.isEmpty()) {
            return Optional.empty();
        } else if (tokens.size() == 1) {
            return Optional.of(new TermQuery(new Term(FIELD_NAME, tokens.get(0))));
        } else {
            return Optional.of(new PhraseQuery(FIELD_NAME, tokens.toArray(new String[0])));
        }

    }

    public Optional<Query> evaluateToQueryAnyAll(Sequence contextSequence, Item contextItem, boolean noMatchOnEmpty) throws XPathException {
        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var queries = new ArrayList<Query>();

        for (var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringifies = iterator.nextItem().getStringValue();
            var maybeQuery = LuceneQueryProducer.stringToQuery(stringifies);
            if (noMatchOnEmpty && maybeQuery.isEmpty()) {
                return Optional.empty();
            }
            maybeQuery.ifPresent(queries::add);
        }

        if (queries.isEmpty()) {
            return Optional.empty();
        } else if (queries.size() == 1) {
            return Optional.of(queries.get(0));
        } else {
            var builder = new BooleanQuery.Builder();
            queries.forEach(x -> builder.add(x, BooleanClause.Occur.SHOULD));
            return Optional.of(builder.build());
        }
    }

    @Override
    public String toString() {
        return "FTExpressionMatch [expression=" + expression + "]";
    }


}
