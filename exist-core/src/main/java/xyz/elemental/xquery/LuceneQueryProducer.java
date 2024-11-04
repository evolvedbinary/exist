/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.exist.xquery.XPathException;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

import java.util.Optional;

public interface LuceneQueryProducer {


    /**
     * Evaluate XQuery FT selection and produce Lucene query to filtering.
     * @param contextSequence
     * @param contextItem
     * @return
     * @throws Exception
     */
    Optional<FtQuery> evaluateToQuery(Sequence contextSequence, Item contextItem) throws XPathException;

}
