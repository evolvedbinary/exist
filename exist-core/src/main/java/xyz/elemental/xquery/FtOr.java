package xyz.elemental.xquery;

import org.apache.lucene.search.Query;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

public class FtOr extends FtBinaryOp {

    public FtOr(FTMatch left, FTMatch right) {
        super(left, right);
    }

    @Override
    public Query evaluateToQuery(Sequence contextSequence, Item contextItem) throws Exception {
        return null;
    }
}
