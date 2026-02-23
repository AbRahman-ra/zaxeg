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
import net.sf.saxon.value.StringValue;

final class TinyCommentImpl
extends TinyNodeImpl {
    public TinyCommentImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public final String getStringValue() {
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
        return 8;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.comment(this.getStringValue(), locationId, 0);
    }
}

