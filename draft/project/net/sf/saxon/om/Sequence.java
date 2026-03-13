/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public interface Sequence {
    public Item head() throws XPathException;

    public SequenceIterator iterate() throws XPathException;

    default public GroundedValue materialize() throws XPathException {
        return this.iterate().materialize();
    }

    default public Sequence makeRepeatable() throws XPathException {
        return this;
    }
}

