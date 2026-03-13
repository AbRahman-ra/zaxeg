/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public interface AtomizedValueIterator
extends SequenceIterator {
    public AtomicSequence nextAtomizedValue() throws XPathException;
}

