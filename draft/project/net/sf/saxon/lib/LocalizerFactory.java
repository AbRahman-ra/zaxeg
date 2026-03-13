/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Properties;
import net.sf.saxon.lib.Numberer;

public abstract class LocalizerFactory {
    public void setLanguageProperties(String lang, Properties properties) {
    }

    public abstract Numberer getNumberer(String var1, String var2);

    public LocalizerFactory copy() {
        return this;
    }
}

