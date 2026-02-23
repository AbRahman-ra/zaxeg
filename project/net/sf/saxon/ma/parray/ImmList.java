/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.parray;

import java.util.List;
import net.sf.saxon.ma.parray.ImmList0;
import net.sf.saxon.ma.parray.ImmList1;
import net.sf.saxon.ma.parray.ImmList2;

public abstract class ImmList<E>
implements Iterable<E> {
    public static <E> ImmList<E> empty() {
        return ImmList0.INSTANCE;
    }

    public static <E> ImmList<E> singleton(E member) {
        return new ImmList1<E>(member);
    }

    public static <E> ImmList<E> pair(E first, E second) {
        return new ImmList2<E>(new ImmList1<E>(first), new ImmList1<E>(second));
    }

    public static <E> ImmList<E> fromList(List<E> members) {
        int size = members.size();
        if (size == 0) {
            return ImmList.empty();
        }
        if (size == 1) {
            return ImmList.singleton(members.get(0));
        }
        int split = size / 2;
        List<E> left = members.subList(0, split);
        List<E> right = members.subList(split, size);
        return new ImmList2<E>(ImmList.fromList(left), ImmList.fromList(right));
    }

    public abstract E get(int var1);

    public E head() {
        return this.get(0);
    }

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract ImmList<E> replace(int var1, E var2);

    public abstract ImmList<E> insert(int var1, E var2);

    public abstract ImmList<E> append(E var1);

    public abstract ImmList<E> appendList(ImmList<E> var1);

    public abstract ImmList<E> remove(int var1);

    public abstract ImmList<E> subList(int var1, int var2);

    public ImmList<E> tail() {
        return this.remove(0);
    }

    protected ImmList<E> rebalance() {
        return this;
    }

    protected IndexOutOfBoundsException outOfBounds(int requested, int actual) {
        return new IndexOutOfBoundsException("Requested " + requested + ", actual size " + actual);
    }
}

