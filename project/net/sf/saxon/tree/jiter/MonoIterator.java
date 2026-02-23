/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.jiter;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MonoIterator<T>
implements Iterator<T> {
    private T thing;
    private boolean gone = false;

    public MonoIterator(T thing) {
        this.thing = thing;
    }

    @Override
    public boolean hasNext() {
        return !this.gone;
    }

    @Override
    public T next() {
        if (this.gone) {
            throw new NoSuchElementException();
        }
        this.gone = true;
        return this.thing;
    }
}

