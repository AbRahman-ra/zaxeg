/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;

public class BasicTransformerFactory
extends TransformerFactoryImpl {
    public BasicTransformerFactory() {
        Configuration config = new Configuration();
        config.setProcessor(this);
        this.setConfiguration(config);
    }

    public BasicTransformerFactory(Configuration config) {
        super(config);
    }
}

