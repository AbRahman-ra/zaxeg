/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.trans.XPathException;

@FunctionalInterface
public interface Action {
    public void doAction() throws XPathException;
}

