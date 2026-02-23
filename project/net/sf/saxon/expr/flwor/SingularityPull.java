/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.TuplePull;

public class SingularityPull
extends TuplePull {
    private boolean done = false;

    @Override
    public boolean nextTuple(XPathContext context) {
        if (this.done) {
            return false;
        }
        this.done = true;
        return true;
    }
}

