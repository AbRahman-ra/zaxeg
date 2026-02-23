/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public interface GroupIterator
extends SequenceIterator {
    public AtomicSequence getCurrentGroupingKey();

    public SequenceIterator iterateCurrentGroup() throws XPathException;
}

