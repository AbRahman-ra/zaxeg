/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.text.CollationKey;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;

public class CollationMatchKey
implements AtomicMatchKey,
Comparable {
    private CollationKey key;

    public CollationMatchKey(CollationKey key) {
        this.key = key;
    }

    public int compareTo(Object o) {
        if (o instanceof CollationMatchKey) {
            return this.key.compareTo(((CollationMatchKey)o).key);
        }
        throw new ClassCastException();
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof CollationMatchKey && this.key.equals(((CollationMatchKey)o).key);
    }

    @Override
    public AtomicValue asAtomic() {
        return new Base64BinaryValue(this.key.toByteArray());
    }
}

