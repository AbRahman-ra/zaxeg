/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.EmptySequence;

public interface Item
extends GroundedValue {
    public Genre getGenre();

    @Override
    default public Item head() {
        return this;
    }

    @Override
    public String getStringValue();

    @Override
    public CharSequence getStringValueCS();

    public AtomicSequence atomize() throws XPathException;

    @Override
    default public String toShortString() {
        return this.toString();
    }

    @Override
    default public Item itemAt(int n) {
        return n == 0 ? this.head() : null;
    }

    @Override
    default public GroundedValue subsequence(int start, int length) {
        return start <= 0 && start + length > 0 ? this : EmptySequence.getInstance();
    }

    @Override
    default public int getLength() {
        return 1;
    }

    @Override
    default public SingletonIterator<? extends Item> iterate() {
        return new SingletonIterator<Item>(this);
    }

    @Override
    default public GroundedValue reduce() {
        return this;
    }

    public static GroundedValue toGroundedValue(Item item) {
        return item.reduce();
    }

    default public boolean isStreamed() {
        return false;
    }
}

