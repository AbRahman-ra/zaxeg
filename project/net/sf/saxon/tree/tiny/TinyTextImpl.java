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
import net.sf.saxon.value.UntypedAtomicValue;

public final class TinyTextImpl
extends TinyNodeImpl {
    public TinyTextImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        int start = this.tree.alpha[this.nodeNr];
        int len = this.tree.beta[this.nodeNr];
        return this.tree.charBuffer.subSequence(start, start + len);
    }

    public static CharSequence getStringValue(TinyTree tree, int nodeNr) {
        int start = tree.alpha[nodeNr];
        int len = tree.beta[nodeNr];
        return tree.charBuffer.subSequence(start, start + len);
    }

    @Override
    public final int getNodeKind() {
        return 3;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.characters(this.getStringValueCS(), locationId, 0);
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return new UntypedAtomicValue(this.getStringValueCS());
    }
}

