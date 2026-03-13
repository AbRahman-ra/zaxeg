/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Collections;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.UnfailingIterator;

public final class EmptySequence<T extends Item>
implements GroundedValue {
    private static EmptySequence THE_INSTANCE = new EmptySequence();
    public Collections a;

    private EmptySequence() {
    }

    public static <T extends Item> EmptySequence<T> getInstance() {
        return THE_INSTANCE;
    }

    @Override
    public String getStringValue() {
        return "";
    }

    @Override
    public CharSequence getStringValueCS() {
        return "";
    }

    public T head() {
        return null;
    }

    @Override
    public UnfailingIterator iterate() {
        return EmptyIterator.emptyIterator();
    }

    public Item asItem() {
        return null;
    }

    @Override
    public final int getLength() {
        return 0;
    }

    public boolean equals(Object other) {
        if (!(other instanceof GroundedValue) || ((GroundedValue)other).getLength() != 0) {
            throw new ClassCastException("Cannot compare " + other.getClass() + " to empty sequence");
        }
        return true;
    }

    public int hashCode() {
        return 42;
    }

    @Override
    public boolean effectiveBooleanValue() {
        return false;
    }

    public T itemAt(int n) {
        return null;
    }

    @Override
    public GroundedValue subsequence(int min, int length) {
        return this;
    }

    public String toString() {
        return "()";
    }

    @Override
    public GroundedValue reduce() {
        return this;
    }
}

