/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.pull.PullConsumer;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.PullPushTee;
import net.sf.saxon.trans.XPathException;

public class PullPushCopier {
    private PullProvider in;
    private Receiver out;

    public PullPushCopier(PullProvider in, Receiver out) {
        this.out = out;
        this.in = in;
    }

    public void copy() throws XPathException {
        this.out.open();
        PullPushTee tee = new PullPushTee(this.in, this.out);
        new PullConsumer(tee).consume();
        this.out.close();
    }

    public void append() throws XPathException {
        PullPushTee tee = new PullPushTee(this.in, this.out);
        new PullConsumer(tee).consume();
    }
}

