/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public abstract class PseudoExpression
extends Expression {
    private void cannotEvaluate() throws XPathException {
        throw new XPathException("Cannot evaluate " + this.getClass().getName());
    }

    @Override
    public int getImplementationMethod() {
        return 0;
    }

    @Override
    protected final int computeCardinality() {
        return 57344;
    }

    @Override
    public ItemType getItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public final Item evaluateItem(XPathContext context) throws XPathException {
        this.cannotEvaluate();
        return null;
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        this.cannotEvaluate();
        return false;
    }

    @Override
    public final CharSequence evaluateAsString(XPathContext context) throws XPathException {
        this.cannotEvaluate();
        return "";
    }

    @Override
    public final SequenceIterator iterate(XPathContext context) throws XPathException {
        this.cannotEvaluate();
        return null;
    }

    @Override
    public void process(Outputter output, XPathContext context) throws XPathException {
        this.cannotEvaluate();
    }
}

