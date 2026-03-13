/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.c14n.implementations;

import java.util.ArrayList;
import java.util.List;
import org.apache.xml.security.c14n.implementations.NameSpaceSymbEntry;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class SymbMap
implements Cloneable {
    int free = 23;
    NameSpaceSymbEntry[] entries = new NameSpaceSymbEntry[this.free];
    String[] keys = new String[this.free];

    SymbMap() {
    }

    void put(String key, NameSpaceSymbEntry value) {
        int index = this.index(key);
        String oldKey = this.keys[index];
        this.keys[index] = key;
        this.entries[index] = value;
        if (!(oldKey != null && oldKey.equals(key) || --this.free != 0)) {
            this.free = this.entries.length;
            int newCapacity = this.free << 2;
            this.rehash(newCapacity);
        }
    }

    List<NameSpaceSymbEntry> entrySet() {
        ArrayList<NameSpaceSymbEntry> a = new ArrayList<NameSpaceSymbEntry>();
        for (int i = 0; i < this.entries.length; ++i) {
            if (this.entries[i] == null || "".equals(this.entries[i].uri)) continue;
            a.add(this.entries[i]);
        }
        return a;
    }

    protected int index(Object obj) {
        String[] set = this.keys;
        int length = set.length;
        int index = (obj.hashCode() & Integer.MAX_VALUE) % length;
        String cur = set[index];
        if (cur == null || cur.equals(obj)) {
            return index;
        }
        --length;
        while ((cur = set[index = index == length ? 0 : ++index]) != null && !cur.equals(obj)) {
        }
        return index;
    }

    protected void rehash(int newCapacity) {
        int oldCapacity = this.keys.length;
        String[] oldKeys = this.keys;
        NameSpaceSymbEntry[] oldVals = this.entries;
        this.keys = new String[newCapacity];
        this.entries = new NameSpaceSymbEntry[newCapacity];
        int i = oldCapacity;
        while (i-- > 0) {
            if (oldKeys[i] == null) continue;
            String o = oldKeys[i];
            int index = this.index(o);
            this.keys[index] = o;
            this.entries[index] = oldVals[i];
        }
    }

    NameSpaceSymbEntry get(String key) {
        return this.entries[this.index(key)];
    }

    protected Object clone() {
        try {
            SymbMap copy = (SymbMap)super.clone();
            copy.entries = new NameSpaceSymbEntry[this.entries.length];
            System.arraycopy(this.entries, 0, copy.entries, 0, this.entries.length);
            copy.keys = new String[this.keys.length];
            System.arraycopy(this.keys, 0, copy.keys, 0, this.keys.length);
            return copy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

