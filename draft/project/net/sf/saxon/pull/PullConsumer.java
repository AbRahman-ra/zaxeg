/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.trans.XPathException;

public class PullConsumer {
    private PullProvider in;

    public PullConsumer(PullProvider in) {
        this.in = in;
    }

    public void consume() throws XPathException {
        while (this.in.next() != PullProvider.Event.END_OF_INPUT) {
        }
    }
}

