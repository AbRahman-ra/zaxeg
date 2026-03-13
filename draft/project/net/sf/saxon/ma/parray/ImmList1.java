/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.parray;

import java.util.Iterator;
import net.sf.saxon.ma.parray.ImmList;
import net.sf.saxon.ma.parray.ImmList2;
import net.sf.saxon.tree.jiter.MonoIterator;

public class ImmList1<E>
extends ImmList<E> {
    private E member;

    public ImmList1(E member) {
        this.member = member;
    }

    @Override
    public E get(int index) {
        if (index == 0) {
            return this.member;
        }
        throw this.outOfBounds(index, 1);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ImmList<E> replace(int index, E member) {
        if (index == 0) {
            return new ImmList1<E>(member);
        }
        throw this.outOfBounds(index, 1);
    }

    @Override
    public ImmList<E> insert(int index, E member) {
        if (index == 0) {
            return new ImmList2<E>(new ImmList1<E>(member), this);
        }
        if (index == 1) {
            return new ImmList2<E>(this, new ImmList1<E>(member));
        }
        throw this.outOfBounds(index, 1);
    }

    @Override
    public ImmList<E> append(E member) {
        return new ImmList2<E>(this, new ImmList1<E>(member));
    }

    @Override
    public ImmList<E> appendList(ImmList<E> members) {
        return new ImmList2<E>(this, members);
    }

    @Override
    public ImmList<E> remove(int index) {
        if (index == 0) {
            return ImmList.empty();
        }
        throw this.outOfBounds(index, 1);
    }

    @Override
    public ImmList<E> subList(int start, int end) {
        if (start != 0) {
            throw this.outOfBounds(start, 1);
        }
        if (end == 0) {
            return ImmList.empty();
        }
        if (end == 1) {
            return this;
        }
        throw this.outOfBounds(end, 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new MonoIterator<E>(this.member);
    }
}

