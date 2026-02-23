/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AtomizedValueIterator;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

public final class AxisAtomizingIterator
implements SequenceIterator {
    private AtomizedValueIterator base;
    private AtomicSequence results = null;
    private int atomicPosition = 0;

    public AxisAtomizingIterator(AtomizedValueIterator base) {
        this.base = base;
    }

    @Override
    public AtomicValue next() throws XPathException {
        while (true) {
            if (this.results != null) {
                if (this.atomicPosition < this.results.getLength()) {
                    return this.results.itemAt(this.atomicPosition++);
                }
                this.results = null;
                continue;
            }
            AtomicSequence atomized = this.base.nextAtomizedValue();
            if (atomized == null) {
                this.results = null;
                return null;
            }
            if (atomized instanceof AtomicValue) {
                this.results = null;
                return (AtomicValue)atomized;
            }
            this.results = atomized;
            this.atomicPosition = 0;
        }
    }

    @Override
    public void close() {
        this.base.close();
    }
}

