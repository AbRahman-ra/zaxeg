/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import java.util.AbstractList;

public class SingletonList
extends AbstractList {
    private final Object element;

    public SingletonList(Object element) {
        this.element = element;
    }

    public int size() {
        return 1;
    }

    public Object get(int index) {
        if (index == 0) {
            return this.element;
        }
        throw new IndexOutOfBoundsException(index + " != 0");
    }
}

