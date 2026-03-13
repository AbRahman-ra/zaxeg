/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

public class HTMLTagHashSet {
    String[] strings;
    int size;

    public HTMLTagHashSet(int size) {
        this.strings = new String[size];
        this.size = size;
    }

    public void add(String s) {
        int hash = (this.hashCode(s) & Integer.MAX_VALUE) % this.size;
        while (true) {
            if (this.strings[hash] == null) {
                this.strings[hash] = s;
                return;
            }
            if (this.strings[hash].equalsIgnoreCase(s)) {
                return;
            }
            hash = (hash + 1) % this.size;
        }
    }

    public boolean contains(String s) {
        int hash = (this.hashCode(s) & Integer.MAX_VALUE) % this.size;
        while (this.strings[hash] != null) {
            if (this.strings[hash].equalsIgnoreCase(s)) {
                return true;
            }
            hash = (hash + 1) % this.size;
        }
        return false;
    }

    private int hashCode(String s) {
        int hash = 0;
        int limit = s.length();
        if (limit > 24) {
            limit = 24;
        }
        for (int i = 0; i < limit; ++i) {
            hash = (hash << 1) + (s.charAt(i) & 0xDF);
        }
        return hash;
    }
}

