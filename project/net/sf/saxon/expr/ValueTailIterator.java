/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.EnumSet;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;

public class ValueTailIterator
implements SequenceIterator,
GroundedIterator,
LookaheadIterator {
    private GroundedValue baseValue;
    private int start;
    private int pos = 0;

    public ValueTailIterator(GroundedValue base, int start) {
        this.baseValue = base;
        this.start = start;
        this.pos = 0;
    }

    @Override
    public Item next() throws XPathException {
        return this.baseValue.itemAt(this.start + this.pos++);
    }

    @Override
    public boolean hasNext() {
        return this.baseValue.itemAt(this.start + this.pos) != null;
    }

    @Override
    public GroundedValue materialize() {
        if (this.start == 0) {
            return this.baseValue;
        }
        return this.baseValue.subsequence(this.start, Integer.MAX_VALUE);
    }

    @Override
    public GroundedValue getResidue() {
        if (this.start == 0 && this.pos == 0) {
            return this.baseValue;
        }
        return this.baseValue.subsequence(this.start + this.pos, Integer.MAX_VALUE);
    }

    @Override
    public EnumSet<SequenceIterator.Property> getProperties() {
        return EnumSet.of(SequenceIterator.Property.LOOKAHEAD, SequenceIterator.Property.GROUNDED);
    }
}

