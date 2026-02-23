/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.util.Iterator;

public class SingleIterator
implements Iterator {
    private boolean first = true;
    private Object object;

    public SingleIterator(Object object) {
        this.object = object;
    }

    public boolean hasNext() {
        return this.first;
    }

    public Object next() {
        Object answer = this.object;
        this.object = null;
        this.first = false;
        return answer;
    }

    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported by this iterator");
    }
}

