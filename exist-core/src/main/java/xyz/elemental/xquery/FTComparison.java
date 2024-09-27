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
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.apache.lucene.index.memory.MemoryIndex;
import org.exist.xquery.value.Type;

import javax.annotation.Nullable;

import static xyz.elemental.xquery.LuceneQueryProducer.FIELD_NAME;

public class FTComparison extends AbstractExpression {

    private static final Analyzer STANDARD_ANALYZER = new StandardAnalyzer();

    private final Expression leftExpression;

    private final FTMatch ftSelection;

    @Nullable
    Float score;

    public FTComparison(XQueryContext context, Expression left, FTMatch ftSelection) {
        super(context);
        this.ftSelection = ftSelection;
        this.leftExpression = left;
    }

    public void clearResult() {
        score = null;
    }

    public Float evalScore(Sequence contextSequence, Item contextItem) throws XPathException {
        if (score != null) {
            return score;
        }
        if (contextItem != null) {
            contextSequence = contextItem.toSequence();
        }

        var left = leftExpression.eval(contextSequence, contextItem);

        if (left.isEmpty()) { //No elements, return 0 ==> cause false.
            score = 0f;
            return score;
        }

        var luceneQuery = ftSelection.evaluateToQuery(contextSequence, contextItem);

        float results = 0;
        var lefIt = left.iterate();
        while (lefIt.hasNext()) {   //TODO - Should we index items first and then query?
            var itemStringValue = lefIt.nextItem().getStringValue();

            var memoryIndex = new MemoryIndex();
            memoryIndex.reset();
            memoryIndex.addField(FIELD_NAME, itemStringValue, STANDARD_ANALYZER);
            results += memoryIndex.search(luceneQuery);
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

    @Override
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        //TODO - Setup dependencies.
    }

    @Override
    public int returnsType() {
        return Type.BOOLEAN;
    }

    @Override
    public void dump(ExpressionDumper dumper) {
        dumper.display("FTExpression - Dump not implemented");
    }

}
