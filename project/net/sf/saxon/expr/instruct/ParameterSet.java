/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Arrays;
import java.util.Map;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Closure;

public class ParameterSet {
    private StructuredQName[] keys;
    private Sequence[] values;
    private boolean[] typeChecked;
    private int used = 0;
    public static ParameterSet EMPTY_PARAMETER_SET = new ParameterSet(0);
    public static final int NOT_SUPPLIED = 0;
    public static final int SUPPLIED = 1;
    public static final int SUPPLIED_AND_CHECKED = 2;

    public ParameterSet() {
        this(10);
    }

    public ParameterSet(int capacity) {
        this.keys = new StructuredQName[capacity];
        this.values = new Sequence[capacity];
        this.typeChecked = new boolean[capacity];
    }

    public ParameterSet(Map<StructuredQName, Sequence> map) {
        this(map.size());
        int i = 0;
        for (Map.Entry<StructuredQName, Sequence> entry : map.entrySet()) {
            this.keys[i] = entry.getKey();
            this.values[i] = entry.getValue();
            this.typeChecked[i++] = false;
        }
        this.used = i;
    }

    public ParameterSet(ParameterSet existing, int extra) {
        this(existing.used + extra);
        for (int i = 0; i < existing.used; ++i) {
            this.put(existing.keys[i], existing.values[i], existing.typeChecked[i]);
        }
    }

    public int size() {
        return this.used;
    }

    public void put(StructuredQName id, Sequence value, boolean checked) {
        for (int i = 0; i < this.used; ++i) {
            if (!this.keys[i].equals(id)) continue;
            this.values[i] = value;
            this.typeChecked[i] = checked;
            return;
        }
        if (this.used + 1 > this.keys.length) {
            int newLength = this.used <= 5 ? 10 : this.used * 2;
            this.values = Arrays.copyOf(this.values, newLength);
            this.keys = Arrays.copyOf(this.keys, newLength);
            this.typeChecked = Arrays.copyOf(this.typeChecked, newLength);
        }
        this.keys[this.used] = id;
        this.typeChecked[this.used] = checked;
        this.values[this.used++] = value;
    }

    public StructuredQName[] getParameterNames() {
        return this.keys;
    }

    public int getIndex(StructuredQName id) {
        for (int i = 0; i < this.used; ++i) {
            if (!this.keys[i].equals(id)) continue;
            return i;
        }
        return -1;
    }

    public Sequence getValue(int index) {
        return this.values[index];
    }

    public boolean isTypeChecked(int index) {
        return this.typeChecked[index];
    }

    public void clear() {
        this.used = 0;
    }

    public void materializeValues() throws XPathException {
        for (int i = 0; i < this.used; ++i) {
            if (!(this.values[i] instanceof Closure)) continue;
            this.values[i] = ((Closure)this.values[i]).reduce();
        }
    }
}

