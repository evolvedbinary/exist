/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.Query;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.util.Optional;

public class FTSelection implements LuceneQueryProducer {

    private final FTMatch ftSelection;

    public FTSelection(final FTMatch ftMatch) {
        this.ftSelection = ftMatch;
    }

    @Override
    public Optional<Query> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException {
        return ftSelection.evaluateToQuery(contextSequence, contextItem);
    }
}
