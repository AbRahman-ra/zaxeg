/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import net.sf.saxon.trans.XPathException;

public interface CharSequenceConsumer {
    default public void open() throws XPathException {
    }

    public CharSequenceConsumer cat(CharSequence var1) throws XPathException;

    default public CharSequenceConsumer cat(char c) throws XPathException {
        return this.cat("" + c);
    }

    default public void close() throws XPathException {
    }
}

