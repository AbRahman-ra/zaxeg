/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.jiter;

import java.util.Iterator;
import java.util.function.Supplier;

public class ConcatenatingIterator<E>
implements Iterator<E> {
    Iterator<? extends E> first;
    Supplier<Iterator<? extends E>> second;
    Iterator<? extends E> active;

    public ConcatenatingIterator(Iterator<? extends E> first, Supplier<Iterator<? extends E>> second) {
        this.first = first;
        this.second = second;
        this.active = first;
    }

    @Override
    public boolean hasNext() {
        if (this.active.hasNext()) {
            return true;
        }
        if (this.active == this.first) {
            this.first = null;
            this.active = this.second.get();
            return this.active.hasNext();
        }
        return false;
    }

    @Override
    public E next() {
        return this.active.next();
    }
}

