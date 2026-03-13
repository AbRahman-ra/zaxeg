/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.trans.XPathException;

public class NoDynamicContextException
extends XPathException {
    public NoDynamicContextException(String message) {
        super("Dynamic context missing: " + message);
    }
}

