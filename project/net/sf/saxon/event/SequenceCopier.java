/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class SequenceCopier {
    private SequenceCopier() {
    }

    public static void copySequence(SequenceIterator in, Receiver out) throws XPathException {
        out.open();
        in.forEachOrFail(out::append);
        out.close();
    }
}

