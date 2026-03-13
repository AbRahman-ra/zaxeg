/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.arrays;

import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntSet;

public interface ArrayItem
extends Function {
    public static final SequenceType SINGLE_ARRAY_TYPE = SequenceType.makeSequenceType(ArrayItemType.ANY_ARRAY_TYPE, 16384);

    public GroundedValue get(int var1);

    public ArrayItem put(int var1, GroundedValue var2);

    public int arrayLength();

    public boolean isEmpty();

    public Iterable<GroundedValue> members();

    public ArrayItem concat(ArrayItem var1);

    public ArrayItem remove(int var1);

    public ArrayItem removeSeveral(IntSet var1);

    public ArrayItem subArray(int var1, int var2);

    public ArrayItem insert(int var1, GroundedValue var2);

    public SequenceType getMemberType(TypeHierarchy var1);

    @Override
    default public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append("array{");
        int count = 0;
        for (GroundedValue member : this.members()) {
            if (count++ > 2) {
                sb.append(" ...");
                break;
            }
            sb.append(member.toShortString());
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    default public Genre getGenre() {
        return Genre.ARRAY;
    }
}

