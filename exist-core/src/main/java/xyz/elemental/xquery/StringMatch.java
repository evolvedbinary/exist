/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.exist.xquery.LiteralValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.StringValue;

import java.io.IOException;
import java.util.ArrayList;

public class StringMatch extends FTMatch {

    private static final Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);



    private final String term;

    public StringMatch(String term) {
        this.term = term;
    }

    public static final StringMatch newInstance(LiteralValue literalValue) {
        //TODO  - It allowed to have String literal or sequence of expressions.
        final var stringValue = (StringValue) literalValue.getValue();
        return new StringMatch(stringValue.getStringValue());
    }

    @Override
    public Query evaluateToQuery(Sequence contextSequence, Item contextItem) {

        var tokens = new ArrayList<String>();

        try (var tokenStream = analyzer.tokenStream(FIELD_NAME, term)) {
            var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            for (tokenStream.reset(); tokenStream.incrementToken(); ) {
                //tokens.add(new TermQuery(new Term(FIELD_NAME, charTermAttribute.toString())));
                tokens.add(charTermAttribute.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (tokens.size() == 1) {
            return new TermQuery(new Term(FIELD_NAME, tokens.get(0)));
        } else {
            return new PhraseQuery(FIELD_NAME, tokens.toArray(new String[0]));
        }
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return this.term;
    }
}
