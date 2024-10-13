/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.*;
import org.exist.xquery.LiteralValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.StringValue;

import java.util.Optional;

public class StringMatch extends FTMatch {

    private final String term;

    public StringMatch(String term) {
        this.term = term;
    }

    public static final StringMatch newInstance(LiteralValue literalValue) {
        final var stringValue = (StringValue) literalValue.getValue();
        return new StringMatch(stringValue.getStringValue());
    }

    @Override
    public Optional<Query> evaluateToQuery(Sequence contextSequence, Item contextItem) {
        return LuceneQueryProducer.stringToQuery(term);
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return this.term;
    }
}