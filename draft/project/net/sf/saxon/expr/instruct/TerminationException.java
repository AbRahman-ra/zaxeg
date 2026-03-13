/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.trans.XPathException;

public class TerminationException
extends XPathException {
    public TerminationException(String message) {
        super(message);
        this.setErrorCode("XTMM9000");
    }
}

