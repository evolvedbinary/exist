/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.lucene.analyzers;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoDiacriticsStandardAnalyzerTest {



    @Test
    public void createComponents() throws Exception {
        var analyzer = new NoDiacriticsStandardAnalyzer();
        //This code will fail on wrong ICU4J dependency
        var result = analyzer.createComponents("myFieldName");
        assertNotNull(result);
    }


}
