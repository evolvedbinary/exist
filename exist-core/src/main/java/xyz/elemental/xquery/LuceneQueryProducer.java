/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.search.Query;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public interface LuceneQueryProducer {

    /**
     * Default field name in lucene index.
     * This field is used when query is created or when In-Memory index is created on text.
     */
    public static final String FIELD_NAME = "data";

    /**
     * Evaluate XQuery FT selection and produce Lucene query to filtering.
     * @param contextSequence
     * @param contextItem
     * @return
     * @throws Exception
     */
    Query evaluateToQuery(Sequence contextSequence, Item contextItem);

}
