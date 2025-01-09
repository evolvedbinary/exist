/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.lucene.analyzers;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class NoDiacriticsStandardAnalyzerTest {

    @Test
    public void createComponents() throws Exception {
        var analyzer = new NoDiacriticsStandardAnalyzer();
        //This code will fail on wrong ICU4J dependency
        var result = analyzer.createComponents("myFieldName");
        assertNotNull(result);
    }

    @Test
    public void testAnalyzerOutputOnGermanText() throws IOException {
        //Don't know why we have here zeroes at the end, but it's same as in old Lucene 4.10
        char[] expectedResult = new char[]{114,117,115,115,101,108,115,104,101,105,109,0,0,0,0,0};

        var analyzer = new NoDiacriticsStandardAnalyzer();
        var tokenStream = analyzer.tokenStream("myField", "Rüsselsheim");

        var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            var token = charTermAttribute.buffer();
            System.out.println(token);
            assertArrayEquals(expectedResult, token);
        }
    }


}