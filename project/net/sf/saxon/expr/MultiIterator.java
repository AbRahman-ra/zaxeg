/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.Iterator;
import java.util.List;

public class MultiIterator<T>
implements Iterator<T> {
    private List<Iterator<T>> array;
    private int current;

    public MultiIterator(List<Iterator<T>> array) {
        this.array = array;
        this.current = 0;
    }

    @Override
    public boolean hasNext() {
        while (this.current < this.array.size()) {
            if (this.array.get(this.current).hasNext()) {
                return true;
            }
            ++this.current;
        }
        return false;
    }

    @Override
    public T next() {
        return this.array.get(this.current).next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

