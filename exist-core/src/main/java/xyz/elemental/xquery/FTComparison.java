/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.exist.xquery.*;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.apache.lucene.index.memory.MemoryIndex;

import javax.annotation.Nullable;

/*
    Using left & right give us free analyze method from BinaryOp.
    Do we need to use BinaryOp? Or we should inherit directly from PathExpr ?
     */
public class FTComparison extends BinaryOp {

    private static final String FIELD_NAME = "data";

    //TODO - Is this thread safe ?
    //     - Context safe?
    //     - Thread Local better ? Who will then delete them ?
    //     - Pool better?

    private static final Analyzer STANDARD_ANALYZER = new StandardAnalyzer();

    public FTComparison(XQueryContext context) {
        super(context);
    }

    public FTComparison(XQueryContext context, Expression left, Expression right) {
        super(context);
        setLeft(left);
        setRight(right);
    }

    @Override
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        super.analyze(contextInfo); //Always call super!!!
    }

    @Nullable
    Float score;

    public void clearResult() {
        score = null;
    }

    public Float evalScore(Sequence contextSequence, Item contextItem) throws XPathException {
        if (score != null) {
            return score;
        }
        //TODO - Most classes do this, Should we also do it. If item is present, probably yes.
        if (contextItem != null) {
            contextSequence = contextItem.toSequence();
        }

        var left = getLeft().eval(contextSequence, contextItem);

        if (left.isEmpty()) { //No elements, return 0 ==> cause false.
            score = 0f;
            return score;
        }

        //Assume it's just string at the moment.
        // Can we some kind of cache this?
        var right = getRight().eval(contextSequence, contextItem);

        var rightString = right.getStringValue();

        float results = 0;
        var lefIt = left.iterate();
        while (lefIt.hasNext()) {
            var itemStringValue = lefIt.nextItem().getStringValue();

            var memoryIndex = new MemoryIndex(); //Instantiate in analyze.
            memoryIndex.reset();
            memoryIndex.addField(FIELD_NAME, itemStringValue, STANDARD_ANALYZER);
            var queryParser = new QueryParser(FIELD_NAME, STANDARD_ANALYZER);

            try {
                results += memoryIndex.search(queryParser.parse(rightString));
            } catch (final ParseException e) {
                throw new XPathException(getRight(), new ErrorCodes.JavaErrorCode(e), "Unable to parse search query");
            }
        }
        score = results / left.getItemCount();
        return score;
    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (score == null) {
            evalScore(contextSequence, contextItem);
            var ret = BooleanValue.valueOf(score > 0);
            score = null;
            return ret;
        }
        return BooleanValue.valueOf(score > 0);
    }

//    public static class ScoredBoolean extends BooleanValue {
//        private float score;
//
//        public ScoredBoolean(final float score, final BooleanValue value) {
//            super(value.getValue());
//            this.score = score;
//        }
//
//        public float getScore() {
//            return score;
//        }
//    }

}
