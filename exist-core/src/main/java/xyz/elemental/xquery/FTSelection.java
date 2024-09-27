package xyz.elemental.xquery;

import org.apache.lucene.search.Query;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class FTSelection implements LuceneQueryProducer {

    private final FTMatch ftSelection;

    public FTSelection(final FTMatch ftMatch) {
        this.ftSelection = ftMatch;
    }

    @Override
    public Query evaluateToQuery(Sequence contextSequence, Item contextItem) {
        return ftSelection.evaluateToQuery(contextSequence, contextItem);
    }
}
