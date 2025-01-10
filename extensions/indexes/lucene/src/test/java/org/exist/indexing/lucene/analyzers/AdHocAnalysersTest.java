/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.lucene.analyzers;
import  org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;

import java.io.IOException;
import java.util.ArrayList;

public class AdHocAnalysersTest {

    private final String TEXT = "Habe nun, ach! Philosophie,";

    @Test
    public void testAnalyzerOutputOnGermanText() throws IOException {

        var analyzer = new WhitespaceAnalyzer();
        var tokenStream = analyzer.tokenStream("myField", TEXT);

        var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        final var results = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            var token = charTermAttribute.toString();
            System.out.println(token);
            results.add(token);
        }
        assertThat(results, hasItem("nun,"));

    }

    @Test
    public void testEnglishAnalyzer() throws IOException {

        var analyzer = new org.apache.lucene.analysis.en.EnglishAnalyzer();
        var tokenStream = analyzer.tokenStream("myField", "some text inside an element");

        var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        final var results = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            var token = charTermAttribute.toString();
            System.out.println(token);
            results.add(token);
        }
    }

    @Test
    public void standardAnalyzerStopwordTest() throws IOException {

        var analyzer = new StandardAnalyzer(org.apache.lucene.analysis.en.EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        //var tokenStream = analyzer.tokenStream("myField", "The stopwords should not be indexed.");


        var tokenStream = analyzer.normalize("myField", "The stopwords should not be indexed.");

    }


}
