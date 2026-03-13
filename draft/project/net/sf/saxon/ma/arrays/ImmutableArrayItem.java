/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import java.util.Arrays;
import net.sf.saxon.ma.arrays.AbstractArrayItem;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.parray.ImmList;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntSet;

public class ImmutableArrayItem
extends AbstractArrayItem {
    private ImmList<GroundedValue> vector;

    public ImmutableArrayItem(SimpleArrayItem other) {
        this.vector = ImmList.fromList(other.getMembers());
    }

    private ImmutableArrayItem(ImmList<GroundedValue> vector) {
        this.vector = vector;
    }

    @Override
    public GroundedValue get(int index) {
        return this.vector.get(index);
    }

    @Override
    public ArrayItem put(int index, GroundedValue newValue) {
        ImmList<GroundedValue> v2 = this.vector.replace(index, newValue);
        return v2 == this.vector ? this : new ImmutableArrayItem(v2);
    }

    @Override
    public ArrayItem insert(int position, GroundedValue member) {
        ImmList<GroundedValue> v2 = this.vector.insert(position, member);
        return new ImmutableArrayItem(v2);
    }

    @Override
    public int arrayLength() {
        return this.vector.size();
    }

    @Override
    public boolean isEmpty() {
        return this.vector.isEmpty();
    }

    @Override
    public Iterable<GroundedValue> members() {
        return this.vector;
    }

    @Override
    public ArrayItem subArray(int start, int end) {
        return new ImmutableArrayItem(this.vector.subList(start, end));
    }

    @Override
    public ArrayItem concat(ArrayItem other) {
        if (other.arrayLength() == 0) {
            return this;
        }
        ImmList<GroundedValue> v1 = other instanceof ImmutableArrayItem ? ((ImmutableArrayItem)other).vector : new ImmutableArrayItem((SimpleArrayItem)((SimpleArrayItem)other)).vector;
        ImmList<GroundedValue> v2 = this.vector.appendList(v1);
        return new ImmutableArrayItem(v2);
    }

    @Override
    public ArrayItem remove(int index) {
        ImmList<GroundedValue> v2 = this.vector.remove(index);
        return v2 == this.vector ? this : new ImmutableArrayItem(v2);
    }

    @Override
    public ArrayItem removeSeveral(IntSet positions) {
        int[] p = new int[positions.size()];
        int i = 0;
        IntIterator ii = positions.iterator();
        while (ii.hasNext()) {
            p[i++] = ii.next();
        }
        Arrays.sort(p);
        ImmList<GroundedValue> v2 = this.vector;
        for (int j = p.length - 1; j >= 0; --j) {
            v2 = v2.remove(p[j]);
        }
        return v2 == this.vector ? this : new ImmutableArrayItem(v2);
    }
}

