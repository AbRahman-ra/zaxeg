/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import net.sf.saxon.value.AtomicValue;

public class ObjectToBeSorted<T> {
    public T value;
    public AtomicValue[] sortKeyValues;
    public int originalPosition;

    public ObjectToBeSorted(int numberOfSortKeys) {
        this.sortKeyValues = new AtomicValue[numberOfSortKeys];
    }
}

