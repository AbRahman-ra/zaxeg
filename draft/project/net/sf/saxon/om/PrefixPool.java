/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PrefixPool {
    String[] prefixes = new String[8];
    int used = 0;
    Map<String, Integer> index = null;

    public PrefixPool() {
        this.prefixes[0] = "";
        this.used = 1;
    }

    public int obtainPrefixCode(String prefix) {
        if (prefix.isEmpty()) {
            return 0;
        }
        if (this.index == null && this.used > 8) {
            this.makeIndex();
        }
        if (this.index != null) {
            Integer existing = this.index.get(prefix);
            if (existing != null) {
                return existing;
            }
        } else {
            for (int i = 0; i < this.used; ++i) {
                if (!this.prefixes[i].equals(prefix)) continue;
                return i;
            }
        }
        int code = this.used++;
        if (this.used >= this.prefixes.length) {
            this.prefixes = Arrays.copyOf(this.prefixes, this.used * 2);
        }
        this.prefixes[code] = prefix;
        if (this.index != null) {
            this.index.put(prefix, code);
        }
        return code;
    }

    private void makeIndex() {
        this.index = new HashMap<String, Integer>(this.used);
        for (int i = 0; i < this.used; ++i) {
            this.index.put(this.prefixes[i], i);
        }
    }

    public String getPrefix(int code) {
        if (code < this.used) {
            return this.prefixes[code];
        }
        throw new IllegalArgumentException("Unknown prefix code " + code);
    }

    public void condense() {
        this.prefixes = Arrays.copyOf(this.prefixes, this.used);
        this.index = null;
    }
}

