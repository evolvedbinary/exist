/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class StringMatchTest {

    @Test
    public void simpleTest() {
        var m = new StringMatch("Hello WorlD");
        var result = m.evaluateToQuery(null, null);
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(x -> assertThat(x).isInstanceOf(PhraseQuery.class));
    }

    @Test
    public void testStopWord() {
        var m = new StringMatch("a as at Hello");
        var result = m.evaluateToQuery(null, null);
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(x -> assertThat(x).isInstanceOf(TermQuery.class));
    }
}
