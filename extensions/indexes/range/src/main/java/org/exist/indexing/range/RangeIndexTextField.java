/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.range;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class RangeIndexTextField extends Field  {

    private static final FieldType FIELD_TYPE = new FieldType();
    private Analyzer analyzer;

    static {
        FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        FIELD_TYPE.setTokenized(true);
        FIELD_TYPE.freeze();
    }

    protected RangeIndexTextField(String name, String value) {
        super(name, value, FIELD_TYPE);
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) {
        if (analyzer != null) {
            return analyzer.tokenStream(this.name, this.stringValue());
        } else {
            return super.tokenStream(analyzer, reuse);
        }
    }
}
