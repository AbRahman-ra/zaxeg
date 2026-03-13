/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.TransformerException;
import net.sf.saxon.Configuration;

public interface Initializer {
    public void initialize(Configuration var1) throws TransformerException;
}

