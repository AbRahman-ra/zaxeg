/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LinkedIterator
implements Iterator {
    private List iterators = new ArrayList();
    private int cur = 0;

    public void addIterator(Iterator i) {
        this.iterators.add(i);
    }

    public boolean hasNext() {
        boolean has = false;
        if (this.cur < this.iterators.size()) {
            has = ((Iterator)this.iterators.get(this.cur)).hasNext();
            if (!has && this.cur < this.iterators.size()) {
                ++this.cur;
                has = this.hasNext();
            }
        } else {
            has = false;
        }
        return has;
    }

    public Object next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return ((Iterator)this.iterators.get(this.cur)).next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

