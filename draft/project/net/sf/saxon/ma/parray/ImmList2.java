/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.parray;

import java.util.Iterator;
import net.sf.saxon.ma.parray.ImmList;
import net.sf.saxon.ma.parray.ImmList1;
import net.sf.saxon.tree.jiter.ConcatenatingIterator;

public class ImmList2<E>
extends ImmList<E> {
    private ImmList<E> left;
    private ImmList<E> right;
    private int size;
    private static final int THRESHOLD = 10;

    protected ImmList2(ImmList<E> left, ImmList<E> right) {
        this.left = left;
        this.right = right;
        this.size = left.size() + right.size();
    }

    @Override
    public E get(int index) {
        if (index < 0) {
            throw this.outOfBounds(index, this.size);
        }
        if (index < this.left.size()) {
            return this.left.get(index);
        }
        if (index < this.size) {
            return this.right.get(index - this.left.size());
        }
        throw this.outOfBounds(index, this.size);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ImmList<E> replace(int index, E member) {
        if (index < 0) {
            throw this.outOfBounds(index, this.size);
        }
        if (index < this.left.size()) {
            return new ImmList2<E>(this.left.replace(index, member), this.right);
        }
        if (index < this.size) {
            return new ImmList2<E>(this.left, this.right.replace(index - this.left.size(), member));
        }
        throw this.outOfBounds(index, this.size);
    }

    @Override
    public ImmList<E> insert(int index, E member) {
        if (index < 0) {
            throw this.outOfBounds(index, this.size);
        }
        if (index <= this.left.size()) {
            return new ImmList2<E>(this.left.insert(index, member), this.right).rebalance();
        }
        if (index <= this.size) {
            return new ImmList2<E>(this.left, this.right.insert(index - this.left.size(), member)).rebalance();
        }
        throw this.outOfBounds(index, this.size);
    }

    @Override
    public ImmList<E> append(E member) {
        return new ImmList2<E>(this, new ImmList1<E>(member)).rebalance();
    }

    @Override
    public ImmList<E> appendList(ImmList<E> members) {
        return new ImmList2<E>(this, members).rebalance();
    }

    @Override
    public ImmList<E> remove(int index) {
        if (index < 0) {
            throw this.outOfBounds(index, this.size);
        }
        if (index < this.left.size()) {
            return new ImmList2<E>(this.left.remove(index), this.right).rebalance();
        }
        if (index < this.size) {
            return new ImmList2<E>(this.left, this.right.remove(index - this.left.size())).rebalance();
        }
        throw this.outOfBounds(index, this.size);
    }

    @Override
    public ImmList<E> subList(int start, int end) {
        if (start < 0 || start >= this.size) {
            throw this.outOfBounds(start, this.size);
        }
        if (end < start || end > this.size) {
            throw this.outOfBounds(end, this.size);
        }
        if (start < this.left.size() && end <= this.left.size()) {
            return this.left.subList(start, end);
        }
        if (start >= this.left.size() && end >= this.left.size()) {
            return this.right.subList(start - this.left.size(), end - this.left.size());
        }
        return new ImmList2<E>(this.left.subList(start, this.left.size()), this.right.subList(0, end - this.left.size())).rebalance();
    }

    @Override
    public Iterator<E> iterator() {
        return new ConcatenatingIterator(this.left.iterator(), () -> this.right.iterator());
    }

    @Override
    protected ImmList<E> rebalance() {
        if (this.left.isEmpty()) {
            return this.right;
        }
        if (this.right.isEmpty()) {
            return this.left;
        }
        ImmList<E> l2 = this.left;
        ImmList<E> r2 = this.right;
        if (this.size() > 10) {
            if (l2 instanceof ImmList2 && l2.size() > 10 * r2.size()) {
                return new ImmList2<E>(((ImmList2)l2).left, new ImmList2<E>(((ImmList2)l2).right, r2));
            }
            if (r2 instanceof ImmList2 && r2.size() > 10 * l2.size()) {
                return new ImmList2<E>(new ImmList2<E>(l2, ((ImmList2)r2).left), ((ImmList2)r2).right);
            }
            return this;
        }
        if (this.left == l2 && this.right == r2) {
            return this;
        }
        return new ImmList2<E>(l2, r2);
    }
}

