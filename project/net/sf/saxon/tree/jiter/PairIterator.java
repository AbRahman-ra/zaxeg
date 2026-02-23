/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.jiter;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PairIterator<T>
implements Iterator<T> {
    private T one;
    private T two;
    private int pos = 0;

    public PairIterator(T one, T two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public boolean hasNext() {
        return this.pos < 2;
    }

    @Override
    public T next() {
        switch (this.pos++) {
            case 0: {
                return this.one;
            }
            case 1: {
                return this.two;
            }
        }
        throw new NoSuchElementException();
    }
}

