/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.StaticQueryContext;

public class StaticQueryContextFactory {
    public StaticQueryContext newStaticQueryContext(Configuration config, boolean copyFromDefault) {
        return new StaticQueryContext(config, copyFromDefault);
    }
}

