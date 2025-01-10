/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class ExistLuceneTextField extends Field  {

    private static final FieldType FIELD_TYPE = new FieldType();
    private static final FieldType FIELD_TYPE_STORED = new FieldType();
    private Analyzer analyzer;

    static {
        FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        FIELD_TYPE.setTokenized(true);
        FIELD_TYPE.setStoreTermVectors(false);
        FIELD_TYPE.freeze();

        FIELD_TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        FIELD_TYPE_STORED.setTokenized(true);
        FIELD_TYPE_STORED.setStoreTermVectors(false);
        FIELD_TYPE_STORED.setStored(true);
        FIELD_TYPE_STORED.freeze();
    }

    /**
     * Create Text field which is analyzed but not stored.
     * @param name
     * @param value
     */
    public ExistLuceneTextField(String name, String value) {
        super(name, value, FIELD_TYPE);
    }

    public ExistLuceneTextField(String name, String value, FieldType fieldType) {
        super(name, value, fieldType);
    }

    public ExistLuceneTextField(String name, String value, Store store) {
        super(name, value, store == Store.YES? FIELD_TYPE_STORED : FIELD_TYPE);
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public TokenStream tokenStream(Analyzer fallBackAnalyzer, TokenStream reuse) {
        if (analyzer != null) {
            return analyzer.tokenStream(this.name, this.stringValue());
        } else {
            return super.tokenStream(fallBackAnalyzer, reuse);
        }
    }
}