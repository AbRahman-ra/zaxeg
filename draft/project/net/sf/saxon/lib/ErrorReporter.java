/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.s9api.XmlProcessingError;

@FunctionalInterface
public interface ErrorReporter {
    public void report(XmlProcessingError var1);
}

