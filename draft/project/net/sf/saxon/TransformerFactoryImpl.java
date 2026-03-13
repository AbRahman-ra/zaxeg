/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.jaxp.SaxonTransformerFactory;

public class TransformerFactoryImpl
extends SaxonTransformerFactory {
    public TransformerFactoryImpl() {
    }

    public TransformerFactoryImpl(Configuration config) {
        super(config);
    }
}

