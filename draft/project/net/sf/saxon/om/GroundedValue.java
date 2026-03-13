/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import net.sf.saxon.expr.SingletonIntersectExpression;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.Chain;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.UnfailingIterator;

public interface GroundedValue
extends Sequence {
    @Override
    public UnfailingIterator iterate();

    public Item itemAt(int var1);

    @Override
    public Item head();

    public GroundedValue subsequence(int var1, int var2);

    public int getLength();

    default public boolean effectiveBooleanValue() throws XPathException {
        return ExpressionTool.effectiveBooleanValue(this.iterate());
    }

    public String getStringValue() throws XPathException;

    public CharSequence getStringValueCS() throws XPathException;

    default public GroundedValue reduce() {
        return this;
    }

    @Override
    default public GroundedValue materialize() {
        return this;
    }

    default public String toShortString() {
        return Err.depictSequence(this).toString();
    }

    default public Iterable<? extends Item> asIterable() {
        return new Iterable<Item>(){

            @Override
            public Iterator<Item> iterator() {
                final UnfailingIterator base = GroundedValue.this.iterate();
                return new Iterator<Item>(){
                    Item pending = null;

                    @Override
                    public boolean hasNext() {
                        this.pending = base.next();
                        return this.pending != null;
                    }

                    @Override
                    public Item next() {
                        return this.pending;
                    }
                };
            }
        };
    }

    default public boolean containsNode(NodeInfo sought) throws XPathException {
        return SingletonIntersectExpression.containsNode(this.iterate(), sought);
    }

    default public GroundedValue concatenate(GroundedValue ... others) {
        ArrayList<GroundedValue> c = new ArrayList<GroundedValue>();
        c.add(this);
        Collections.addAll(c, others);
        return new Chain(c);
    }
}

