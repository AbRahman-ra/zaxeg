/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.value.Closure;

public class SingletonClosure
extends Closure
implements Sequence {
    private boolean built = false;
    private Item value = null;

    public SingletonClosure(Expression exp, XPathContext context) throws XPathException {
        this.expression = exp;
        this.savedXPathContext = context.newContext();
        this.saveContext(exp, context);
    }

    @Override
    public UnfailingIterator iterate() throws XPathException {
        return SingletonIterator.makeIterator(this.asItem());
    }

    public Item asItem() throws XPathException {
        if (!this.built) {
            this.value = this.expression.evaluateItem(this.savedXPathContext);
            this.built = true;
            this.savedXPathContext = null;
        }
        return this.value;
    }

    public Item itemAt(int n) throws XPathException {
        if (n != 0) {
            return null;
        }
        return this.asItem();
    }

    public int getLength() throws XPathException {
        return this.asItem() == null ? 0 : 1;
    }

    @Override
    public ZeroOrOne materialize() throws XPathException {
        return new ZeroOrOne<Item>(this.asItem());
    }

    @Override
    public SingletonClosure makeRepeatable() {
        return this;
    }
}

