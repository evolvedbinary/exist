/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */

package org.exist.indexing.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

public class BoostField extends Field {

    public static final org.apache.lucene.document.FieldType BOOST_FIELD_TYPE = new org.apache.lucene.document.FieldType();

    static {
        BOOST_FIELD_TYPE.setIndexOptions(IndexOptions.NONE);
        BOOST_FIELD_TYPE.setStored(true);
        BOOST_FIELD_TYPE.setTokenized(false);
        BOOST_FIELD_TYPE.setStoreTermVectors(false);
        BOOST_FIELD_TYPE.setDocValuesType(DocValuesType.NUMERIC);
        BOOST_FIELD_TYPE.freeze();
    }

    public BoostField(String name, float value) {
        super(name, BOOST_FIELD_TYPE);
        //NumericDocValues in FieldValuesSourceWithFallback can work only with int/long.
        // We need to convert to bits and then back. Otherwise, implicit conversion float->int is applied.
        this.fieldsData = Float.floatToIntBits(value);
    }
}
