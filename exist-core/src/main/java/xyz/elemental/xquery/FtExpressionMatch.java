/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.exist.xquery.Expression;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import xyz.elemental.xquery.analyzer.ExistFtSearchAnalyzer;
import xyz.elemental.xquery.options.MatchOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


public class FtExpressionMatch extends FtSelection {

    final Expression expression;
    private Optional<AnyAllOptions> anyAllOptions;

    private MatchOptions matchOptions;

    public FtExpressionMatch(Expression expression, AnyAllOptions anyAllOptions) {
        this.expression = expression;

        this.anyAllOptions = Optional.ofNullable(anyAllOptions);
    }

    @Override
    public Optional<FtQuery> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException {
        return switch (anyAllOptions.orElse(AnyAllOptions.ANY)) {
            case ANY -> evaluateToQueryAnyAll(contextSequence, contextItem, false);
            case ALL -> evaluateToQueryAnyAll(contextSequence, contextItem, true);
            case PHRASE -> evaluateToQueryPhrase(contextSequence, contextItem);
            case ANY_WORD -> evaluateToQueryAnyAllWords(contextSequence, contextItem, BooleanClause.Occur.SHOULD);
            case ALL_WORDS -> evaluateToQueryAnyAllWords(contextSequence, contextItem, BooleanClause.Occur.MUST);
        };
    }

    public Optional<FtQuery> evaluateToQueryAnyAllWords(Sequence contextSequence, Item contextItem, BooleanClause.Occur occur) throws XPathException {

        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var tokens = new HashSet<String>();

        var query = new FtQuery();
        query.getMatchOptions().add(matchOptions);
        var fieldName = matchOptions.fieldName();
        var analyzer = new ExistFtSearchAnalyzer(matchOptions);

        for (var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringValue = iterator.nextItem().getStringValue();
            tokens.addAll(tokenize(stringValue, analyzer, fieldName));
        }

        if (tokens.isEmpty()) {
            return Optional.empty();
        } else if (tokens.size() == 1) {
            query.setLuceneQuery(new TermQuery(new Term(fieldName, tokens.stream().findFirst().get())));
            return Optional.of(query);
        } else {
            var builder = new BooleanQuery.Builder();
            tokens.stream().forEach(token -> {
                builder.add(new TermQuery(new Term(fieldName, token)), occur);
            });
            query.setLuceneQuery(builder.build());
            return Optional.of(query);
        }

    }

    public Optional<FtQuery> evaluateToQueryPhrase(Sequence contextSequence, Item contextItem) throws XPathException {
        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var query = new FtQuery();
        query.getMatchOptions().add(matchOptions);
        var fieldName = matchOptions.fieldName();
        var analyzer = new ExistFtSearchAnalyzer(matchOptions);

        var tokens = new ArrayList<String>();

        for (var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringValue = iterator.nextItem().getStringValue();
            tokens.addAll(tokenize(stringValue, analyzer, fieldName));
        }

        if (tokens.isEmpty()) {
            return Optional.empty();
        } else if (tokens.size() == 1) {
            query.setLuceneQuery(new TermQuery(new Term(fieldName, tokens.get(0))));
            return Optional.of(query);
        } else {
            query.setLuceneQuery(new PhraseQuery(fieldName, tokens.toArray(new String[0])));
            return Optional.of(query);
        }

    }

    public Optional<FtQuery> evaluateToQueryAnyAll(Sequence contextSequence, Item contextItem, boolean noMatchOnEmpty) throws XPathException {
        var exprSeqResult = expression.eval(contextSequence, contextItem);

        var query = new FtQuery();
        query.getMatchOptions().add(matchOptions);
        var fieldName = matchOptions.fieldName();
        var analyzer = new ExistFtSearchAnalyzer(matchOptions);


        var queries = new ArrayList<Query>();

        for (var iterator = exprSeqResult.iterate(); iterator.hasNext(); ) {
            var stringifies = iterator.nextItem().getStringValue();
            var maybeQuery = stringToQuery(stringifies, analyzer, fieldName);
            if (noMatchOnEmpty && maybeQuery.isEmpty()) {
                return Optional.empty();
            }
            maybeQuery.ifPresent(queries::add);
        }

        if (queries.isEmpty()) {
            return Optional.empty();
        } else if (queries.size() == 1) {
            query.setLuceneQuery(queries.get(0));
            return Optional.of(query);
        } else {
            var builder = new BooleanQuery.Builder();
            queries.forEach(x -> builder.add(x, BooleanClause.Occur.SHOULD));
            query.setLuceneQuery(builder.build());
            return Optional.of(query);
        }
    }

    public static Optional<Query> stringToQuery(String phrase, Analyzer analyzer, String fieldName) {
        var tokens = tokenize(phrase, analyzer, fieldName);

        if(tokens.isEmpty()) {
            return Optional.empty();
        }else {
            if (tokens.size() == 1) {
                return Optional.of(new TermQuery(new Term(fieldName, tokens.get(0))));
            } else {
                return Optional.of(new PhraseQuery(fieldName, tokens.toArray(new String[0])));
            }
        }
    }

    public static List<String> tokenize(String phrase, Analyzer analyzer, String fieldName) {
        var tokens = new ArrayList<String>();
        try (var tokenStream = analyzer.tokenStream(fieldName, phrase)) {
            var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            for (tokenStream.reset(); tokenStream.incrementToken(); ) {
                tokens.add(charTermAttribute.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to tokenize String", e);
        }

        return tokens;
    }

    @Override
    public String toString() {
        return "FTExpressionMatch [expression=" + expression + "]";
    }

    public void setAnyAllOptions(Optional<AnyAllOptions> anyAllOptions) {
        this.anyAllOptions = anyAllOptions;
    }

    public MatchOptions getMatchOptions() {
        return matchOptions;
    }

    public void setMatchOptions(MatchOptions matchOptions) {
        this.matchOptions = matchOptions;
    }
}
