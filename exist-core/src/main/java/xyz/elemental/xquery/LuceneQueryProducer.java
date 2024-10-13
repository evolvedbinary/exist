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
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public interface LuceneQueryProducer {

    /**
     * Default field name in lucene index.
     * This field is used when query is created or when In-Memory index is created on text.
     */
    public static final String FIELD_NAME = "data";

    /**
     * Default Analyzer, in the future, this need to be setup in context because FT should support different
     * languages and set of stop words.
     * TODO - Implement Closeable, should be probably closed on exit.
     *
     */
    public static final Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);

    public static Optional<Query> stringToQuery(String phrase) {
        var tokens = new ArrayList<String>();

        try (var tokenStream = analyzer.tokenStream(FIELD_NAME, phrase)) {
            var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            for (tokenStream.reset(); tokenStream.incrementToken(); ) {
                tokens.add(charTermAttribute.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(tokens.isEmpty()) {
            return Optional.empty();
        }else {
            if (tokens.size() == 1) {
                return Optional.of(new TermQuery(new Term(FIELD_NAME, tokens.get(0))));
            } else {
                return Optional.of(new PhraseQuery(FIELD_NAME, tokens.toArray(new String[0])));
            }
        }
    }

    /**
     * Evaluate XQuery FT selection and produce Lucene query to filtering.
     * @param contextSequence
     * @param contextItem
     * @return
     * @throws Exception
     */
    Optional<Query> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException;

}
