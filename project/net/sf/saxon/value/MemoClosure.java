/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MemoSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Closure;

public class MemoClosure
extends Closure
implements ContextOriginator {
    private Sequence sequence;

    public MemoClosure() {
    }

    public MemoClosure(Expression expr, XPathContext context) throws XPathException {
        this.setExpression(expr);
        XPathContextMajor c2 = context.newContext();
        c2.setOrigin(this);
        this.setSavedXPathContext(c2);
        this.saveContext(expr, context);
    }

    @Override
    public synchronized SequenceIterator iterate() throws XPathException {
        this.makeSequence();
        return this.sequence.iterate();
    }

    private void makeSequence() throws XPathException {
        if (this.sequence == null) {
            this.inputIterator = this.expression.iterate(this.savedXPathContext);
            this.sequence = SequenceTool.toMemoSequence(this.inputIterator);
        }
    }

    public synchronized Item itemAt(int n) throws XPathException {
        this.makeSequence();
        if (this.sequence instanceof GroundedValue) {
            return ((GroundedValue)this.sequence).itemAt(n);
        }
        if (this.sequence instanceof MemoSequence) {
            return ((MemoSequence)this.sequence).itemAt(n);
        }
        throw new IllegalStateException();
    }

    @Override
    public GroundedValue reduce() throws XPathException {
        if (this.sequence instanceof GroundedValue) {
            return (GroundedValue)this.sequence;
        }
        return this.iterate().materialize();
    }

    @Override
    public Sequence makeRepeatable() {
        return this;
    }
}

