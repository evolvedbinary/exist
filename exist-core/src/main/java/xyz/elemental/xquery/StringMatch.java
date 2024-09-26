package xyz.elemental.xquery;

import org.apache.lucene.search.Query;
import org.exist.xquery.LiteralValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.StringValue;

public class StringMatch extends FTMatch {

    private final String term;

    public StringMatch(String term) {
        this.term = term;
    }

    public static final StringMatch newInstance(LiteralValue literalValue) {
        //TODO hacky - is it allowed to have $value or reference any other xpath ??
        final var stringValue = (StringValue)literalValue.getValue();
        return new StringMatch(stringValue.getStringValue());
    }

    @Override
    public Query evaluateToQuery(Sequence contextSequence, Item contextItem) throws Exception {
        return null;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return this.term;
    }
}
