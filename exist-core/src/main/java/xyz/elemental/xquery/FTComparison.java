/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package xyz.elemental.xquery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.exist.xquery.*;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.apache.lucene.index.memory.MemoryIndex;
import org.exist.xquery.value.Type;
import xyz.elemental.xquery.analyzer.ExistFtSearchAnalyzer;
import xyz.elemental.xquery.options.MatchOptions;

import javax.annotation.Nullable;

public class FTComparison extends AbstractExpression {

    private final Expression leftExpression;

    private final FtSelection ftSelection;

    @Nullable
    Float score;

    public FTComparison(XQueryContext context, Expression left, FtSelection ftSelection) {
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

        var maybeFtQuery = ftSelection.evaluateToQuery(contextSequence, contextItem);

        if (maybeFtQuery.isEmpty()) {    //If the sequence is empty, the FTWords yields no matches, Section 3.2
            score = 0f;
            return 0f;
        }

        float results = 0;
        var lefIt = left.iterate();
        var ftQuery = maybeFtQuery.get();
        while (lefIt.hasNext()) {   //TODO - Should we index items first and then query?
            var itemStringValue = lefIt.nextItem().getStringValue();

            var memoryIndex = new MemoryIndex();
            memoryIndex.reset();

            for (MatchOptions matchOptions: ftQuery.getMatchOptions()) {
                var fieldName = matchOptions.fieldName();
                memoryIndex.addField(fieldName, itemStringValue, new ExistFtSearchAnalyzer(matchOptions));
            }

            results += memoryIndex.search(ftQuery.getLuceneQuery());
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
        //TODO - Compile Lucene query when possible.
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
