/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.trans.XPathException;

@FunctionalInterface
public interface LastPositionFinder {
    public int getLength() throws XPathException;
}

