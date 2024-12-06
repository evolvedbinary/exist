/*
 * Copyright (C) 2024 Evolved Binary Ltd
 *
 * This code is proprietary and is not Open Source.
 */
package org.exist.indexing.range;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;

import java.io.IOException;

public class RangeIndexTextField extends Field {

    public static final FieldType TYPE_NOT_STORED = new FieldType();

    /** Indexed, tokenized, stored. */
    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_NOT_STORED.setIndexed(true);
        TYPE_NOT_STORED.setTokenized(true);
        TYPE_NOT_STORED.freeze();

        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.freeze();
    }

    //To overcome problem with analyzers
    private Analyzer analyzer;

    public RangeIndexTextField(String name, String value, Store store) {
        super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
    }

    @Override
    public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) throws IOException {
        if(this.analyzer != null) {
            return this.analyzer.tokenStream(this.name, this.stringValue());
        } else {
            return super.tokenStream(analyzer, reuse);
        }
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }
}
