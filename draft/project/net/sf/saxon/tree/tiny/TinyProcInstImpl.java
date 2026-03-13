/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.StringValue;

final class TinyProcInstImpl
extends TinyNodeImpl {
    public TinyProcInstImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public String getStringValue() {
        int start = this.tree.alpha[this.nodeNr];
        int len = this.tree.beta[this.nodeNr];
        if (len == 0) {
            return "";
        }
        char[] dest = new char[len];
        this.tree.commentBuffer.getChars(start, start + len, dest, 0);
        return new String(dest, 0, len);
    }

    @Override
    public AtomicSequence atomize() {
        return new StringValue(this.getStringValue());
    }

    @Override
    public final int getNodeKind() {
        return 7;
    }

    @Override
    public String getBaseURI() {
        return Navigator.getBaseURI(this);
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.processingInstruction(this.getDisplayName(), this.getStringValue(), locationId, 0);
    }

    public String getTarget() {
        return this.getDisplayName();
    }

    public String getData() {
        return this.getStringValue();
    }
}

