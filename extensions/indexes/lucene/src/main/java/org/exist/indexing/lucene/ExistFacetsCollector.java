/*
 *  Copyright (C) 2024 Evolved Binary Ltd
 *
 *  This code is proprietary and is not Open Source.
 */
package org.exist.indexing.lucene;

import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.index.LeafReaderContext;

import java.io.IOException;

public class ExistFacetsCollector extends FacetsCollector {

    @Override
    public void doSetNextReader(LeafReaderContext context) throws IOException {
        super.doSetNextReader(context);
    }
}
