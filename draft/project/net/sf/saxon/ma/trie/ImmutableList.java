/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.trie;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ImmutableList<T>
implements Iterable<T> {
    private static final EmptyList EMPTY_LIST = new EmptyList();

    public static <T> ImmutableList<T> empty() {
        return EMPTY_LIST;
    }

    public abstract T head();

    public abstract ImmutableList<T> tail();

    public abstract boolean isEmpty();

    public final int size() {
        ImmutableList<T> input = this;
        int size = 0;
        while (!input.isEmpty()) {
            ++size;
            input = input.tail();
        }
        return size;
    }

    public ImmutableList<T> prepend(T element) {
        return new NonEmptyList(element, this);
    }

    public boolean equals(Object o) {
        if (!(o instanceof ImmutableList)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        Iterator<T> thisIter = this.iterator();
        Iterator<T> otherIter = ((ImmutableList)o).iterator();
        while (thisIter.hasNext() && otherIter.hasNext()) {
            if (thisIter.next().equals(otherIter.next())) continue;
            return false;
        }
        return thisIter.hasNext() == otherIter.hasNext();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>(){
            private ImmutableList<T> list;
            {
                this.list = ImmutableList.this;
            }

            @Override
            public boolean hasNext() {
                return !this.list.isEmpty();
            }

            @Override
            public T next() {
                Object element = this.list.head();
                this.list = this.list.tail();
                return element;
            }
        };
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (T elem : this) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(elem);
            first = false;
        }
        builder.append(']');
        return builder.toString();
    }

    public ImmutableList<T> reverse() {
        ImmutableList<T> result = ImmutableList.empty();
        for (T element : this) {
            result = result.prepend(element);
        }
        return result;
    }

    private static class NonEmptyList<T>
    extends ImmutableList<T> {
        private final T element;
        private final ImmutableList<T> tail;

        private NonEmptyList(T element, ImmutableList<T> tail) {
            this.element = element;
            this.tail = tail;
        }

        @Override
        public T head() {
            return this.element;
        }

        @Override
        public ImmutableList<T> tail() {
            return this.tail;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private static class EmptyList
    extends ImmutableList {
        private EmptyList() {
        }

        public Object head() {
            throw new NoSuchElementException("head() called on empty list");
        }

        public ImmutableList tail() {
            throw new NoSuchElementException("head() called on empty list");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }
}

