/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.lucene.boost;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.DoubleValues;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.util.Objects;

public class FieldValuesSourceWithFallback extends DoubleValuesSource {

    private final String field;
    private final float defaultValue;


    private FieldValuesSourceWithFallback(String field, float defaultValue) {
        this.field = field;
        this.defaultValue = defaultValue;
    }

    /**
     * Create new instance of DoubleValuesSource for field with default value.
     * @param field field in Lucene document which contains the value. For example boost.
     * @param defaultValue default value if filed is mission on Lucene document.
     * @return new instance FieldValuesSourceWithFallback
     */
    public static FieldValuesSourceWithFallback newInstance(String field, float defaultValue) {
        return new FieldValuesSourceWithFallback(field, defaultValue);
    }

    /**
     * Create new instance of DoubleValuesSource for field with default value 1.0f.
     * @param field field in Lucene document which contains the value. For example boost.
     * @return new instance FieldValuesSourceWithFallback
     */
    public static FieldValuesSourceWithFallback newInstance(String field) {
        return new FieldValuesSourceWithFallback(field, 1.0f);
    }

    @Override
    public DoubleValues getValues(LeafReaderContext ctx, DoubleValues scores) throws IOException {
        final NumericDocValues fieldValue = DocValues.getNumeric(ctx.reader(), field);


        return new DoubleValues() {
            private boolean hasValue = false;

            @Override
            public double doubleValue() throws IOException {
                if (hasValue) {
                    return Float.intBitsToFloat((int)fieldValue.longValue());
                } else {
                    return defaultValue;
                }
            }

            @Override
            public boolean advanceExact(int doc) throws IOException {
                hasValue = fieldValue.advanceExact(doc);
                return true;
            }
        };
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    @Override
    public DoubleValuesSource rewrite(IndexSearcher reader) throws IOException {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldValuesSourceWithFallback that = (FieldValuesSourceWithFallback) o;
        return Float.compare(defaultValue, that.defaultValue) == 0 && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, defaultValue);
    }

    @Override
    public String toString() {
        return "FieldValuesSourceWithFallback{" +
                "field='" + field + '\'' +
                ", defaultValue=" + defaultValue +
                '}';
    }

    @Override
    public boolean isCacheable(LeafReaderContext ctx) {
        return DocValues.isCacheable(ctx, field);
    }
}
