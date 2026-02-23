/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.om.SequenceIterator;

public interface ReversibleIterator
extends SequenceIterator {
    public SequenceIterator getReverseIterator();
}

