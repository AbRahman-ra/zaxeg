/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class FilterIterator
implements Iterator {
    protected Iterator proxy;
    private Object next;
    private boolean first = true;

    public FilterIterator(Iterator proxy) {
        this.proxy = proxy;
    }

    public boolean hasNext() {
        if (this.first) {
            this.next = this.findNext();
            this.first = false;
        }
        return this.next != null;
    }

    public Object next() throws NoSuchElementException {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        Object answer = this.next;
        this.next = this.findNext();
        return answer;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract boolean matches(Object var1);

    protected Object findNext() {
        if (this.proxy != null) {
            while (this.proxy.hasNext()) {
                Object nextObject = this.proxy.next();
                if (nextObject == null || !this.matches(nextObject)) continue;
                return nextObject;
            }
            this.proxy = null;
        }
        return null;
    }
}

