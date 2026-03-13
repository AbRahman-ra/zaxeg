/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Arrays;
import java.util.StringTokenizer;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.AtomicValue;

public class NestedIntegerValue
extends AtomicValue
implements Comparable,
AtomicMatchKey {
    public static NestedIntegerValue ONE = new NestedIntegerValue(new int[]{1});
    public static NestedIntegerValue TWO = new NestedIntegerValue(new int[]{2});
    int[] value;

    public NestedIntegerValue(String v) throws XPathException {
        this.typeLabel = BuiltInAtomicType.STRING;
        NestedIntegerValue.parse(v);
    }

    public NestedIntegerValue(int[] val) {
        this.typeLabel = BuiltInAtomicType.STRING;
        this.value = val;
    }

    public static NestedIntegerValue parse(String v) throws XPathException {
        StringTokenizer st = new StringTokenizer(v, ".");
        int[] valuei = new int[st.countTokens()];
        try {
            int i = 0;
            while (st.hasMoreTokens()) {
                valuei[i] = Integer.parseInt(st.nextToken());
                ++i;
            }
        } catch (NumberFormatException exc) {
            throw new XPathException("Nested integer value has incorrect format: " + v);
        }
        return new NestedIntegerValue(valuei);
    }

    public NestedIntegerValue append(int leaf) {
        int[] v = new int[this.value.length + 1];
        System.arraycopy(this.value, 0, v, 0, this.value.length);
        v[this.value.length] = leaf;
        return new NestedIntegerValue(v);
    }

    public NestedIntegerValue getStem() {
        if (this.value.length == 0) {
            return null;
        }
        int[] v = new int[this.value.length - 1];
        System.arraycopy(this.value, 0, v, 0, v.length);
        return new NestedIntegerValue(v);
    }

    public int getDepth() {
        return this.value.length;
    }

    public int getLeaf() {
        if (this.value.length == 0) {
            return -1;
        }
        return this.value[this.value.length - 1];
    }

    @Override
    public Comparable getSchemaComparable() {
        return this;
    }

    @Override
    public AtomicMatchKey getXPathComparable(boolean ordered, StringCollator collator, int implicitTimezone) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NestedIntegerValue && Arrays.equals(this.value, ((NestedIntegerValue)o).value);
    }

    @Override
    public BuiltInAtomicType getPrimitiveType() {
        return BuiltInAtomicType.STRING;
    }

    @Override
    public AtomicValue copyAsSubType(AtomicType typeLabel) {
        NestedIntegerValue v = new NestedIntegerValue(this.value);
        v.typeLabel = typeLabel;
        return v;
    }

    @Override
    protected CharSequence getPrimitiveStringValue() {
        FastStringBuffer buffer = new FastStringBuffer(this.value.length * 2);
        for (int i = 0; i < this.value.length - 1; ++i) {
            buffer.append(this.value[i] + ".");
        }
        buffer.append(this.value[this.value.length - 1] + "");
        return buffer;
    }

    public int compareTo(Object other) {
        if (!(other instanceof NestedIntegerValue)) {
            throw new ClassCastException("NestedIntegerValue is not comparable to " + other.getClass());
        }
        NestedIntegerValue v2 = (NestedIntegerValue)other;
        for (int i = 0; i < this.value.length && i < v2.value.length; ++i) {
            if (this.value[i] == v2.value[i]) continue;
            if (this.value[i] < v2.value[i]) {
                return -1;
            }
            return 1;
        }
        return Integer.signum(this.value.length - v2.value.length);
    }
}

