/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.jiter;

import java.util.Iterator;
import java.util.function.Function;

public class MappingJavaIterator<S, T>
implements Iterator<T> {
    private Iterator<S> input;
    private Function<S, T> mapper;

    public MappingJavaIterator(Iterator<S> in, Function<S, T> mapper) {
        this.input = in;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext() {
        return this.input.hasNext();
    }

    @Override
    public T next() {
        T next;
        while ((next = this.mapper.apply(this.input.next())) == null) {
        }
        return next;
    }

    @Override
    public void remove() {
        this.input.remove();
    }
}

