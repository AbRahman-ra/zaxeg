/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public interface GroundedIterator
extends SequenceIterator {
    @Override
    public GroundedValue materialize() throws XPathException;

    public GroundedValue getResidue() throws XPathException;
}

