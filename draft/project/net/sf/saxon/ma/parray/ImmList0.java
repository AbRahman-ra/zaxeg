/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.parray;

import java.util.Collections;
import java.util.Iterator;
import net.sf.saxon.ma.parray.ImmList;
import net.sf.saxon.ma.parray.ImmList1;

public class ImmList0<E>
extends ImmList<E> {
    static final ImmList0 INSTANCE = new ImmList0();

    private ImmList0() {
    }

    @Override
    public E get(int index) {
        throw this.outOfBounds(index, 0);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ImmList<E> replace(int index, E member) {
        throw this.outOfBounds(index, 0);
    }

    @Override
    public ImmList<E> insert(int index, E member) {
        if (index == 0) {
            return new ImmList1<E>(member);
        }
        throw this.outOfBounds(index, 0);
    }

    @Override
    public ImmList<E> append(E member) {
        return new ImmList1<E>(member);
    }

    @Override
    public ImmList<E> appendList(ImmList<E> members) {
        return members;
    }

    @Override
    public ImmList<E> remove(int index) {
        throw this.outOfBounds(index, 0);
    }

    @Override
    public ImmList<E> subList(int start, int end) {
        if (start == 0 && end == 0) {
            return this;
        }
        throw this.outOfBounds(0, 0);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }
}

