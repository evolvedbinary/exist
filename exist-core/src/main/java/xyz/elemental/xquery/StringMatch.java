package xyz.elemental.xquery;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
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
        //TODO  - Check EBNF if it's allowed to have $value or //xpath.
        final var stringValue = (StringValue)literalValue.getValue();
        return new StringMatch(stringValue.getStringValue());
    }

    @Override
    public Query evaluateToQuery(Sequence contextSequence, Item contextItem) {
        return new TermQuery(new Term(FIELD_NAME, term));
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return this.term;
    }
}
