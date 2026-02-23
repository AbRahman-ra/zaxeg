/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.UntypedAtomicValue;

public final class WhitespaceTextImpl
extends TinyNodeImpl {
    public WhitespaceTextImpl(TinyTree tree, int nodeNr) {
        this.tree = tree;
        this.nodeNr = nodeNr;
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        long value = (long)this.tree.alpha[this.nodeNr] << 32 | (long)this.tree.beta[this.nodeNr] & 0xFFFFFFFFL;
        return new CompressedWhitespace(value);
    }

    public static CharSequence getStringValueCS(TinyTree tree, int nodeNr) {
        long value = (long)tree.alpha[nodeNr] << 32 | (long)tree.beta[nodeNr] & 0xFFFFFFFFL;
        return new CompressedWhitespace(value);
    }

    public static void appendStringValue(TinyTree tree, int nodeNr, FastStringBuffer buffer) {
        long value = (long)tree.alpha[nodeNr] << 32 | (long)tree.beta[nodeNr] & 0xFFFFFFFFL;
        CompressedWhitespace.uncompress(value, buffer);
    }

    @Override
    public AtomicSequence atomize() {
        return new UntypedAtomicValue(this.getStringValueCS());
    }

    public static long getLongValue(TinyTree tree, int nodeNr) {
        return (long)tree.alpha[nodeNr] << 32 | (long)tree.beta[nodeNr] & 0xFFFFFFFFL;
    }

    @Override
    public final int getNodeKind() {
        return 3;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.characters(this.getStringValueCS(), locationId, 0);
    }
}

