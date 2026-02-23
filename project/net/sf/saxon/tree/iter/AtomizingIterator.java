/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.iter;

import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

public class AtomizingIterator
implements SequenceIterator {
    private SequenceIterator base;
    private AtomicSequence currentValue = null;
    private int currentValuePosition = 1;
    private int currentValueSize = 1;
    private RoleDiagnostic roleDiagnostic;

    public AtomizingIterator(SequenceIterator base) {
        this.base = base;
    }

    public void setRoleDiagnostic(RoleDiagnostic role) {
        this.roleDiagnostic = role;
    }

    @Override
    public AtomicValue next() throws XPathException {
        while (true) {
            Item nextSource;
            if (this.currentValue != null) {
                if (this.currentValuePosition < this.currentValueSize) {
                    return this.currentValue.itemAt(this.currentValuePosition++);
                }
                this.currentValue = null;
            }
            if ((nextSource = this.base.next()) == null) break;
            try {
                AtomicSequence v = nextSource.atomize();
                if (v instanceof AtomicValue) {
                    return (AtomicValue)v;
                }
                this.currentValue = v;
                this.currentValuePosition = 0;
                this.currentValueSize = this.currentValue.getLength();
            } catch (XPathException e) {
                if (this.roleDiagnostic == null) {
                    throw e;
                }
                String message = e.getMessage() + ". Failed while atomizing the " + this.roleDiagnostic.getMessage();
                throw new XPathException(message, e.getErrorCodeLocalPart(), e.getLocator());
            }
        }
        this.currentValue = null;
        return null;
    }

    @Override
    public void close() {
        this.base.close();
    }
}

