package xyz.elemental.xquery;

import org.apache.lucene.search.Query;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public interface LuceneQueryProducer {

    /**
     * Evaluate XQuery FT selection and produce Lucene query to filtering.
     * @param contextSequence
     * @param contextItem
     * @return
     * @throws Exception
     */
    Query evaluateToQuery(Sequence contextSequence, Item contextItem) throws Exception;

}
