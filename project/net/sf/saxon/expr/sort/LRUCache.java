/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {
    private Map<K, V> map;

    public LRUCache(int cacheSize) {
        this(cacheSize, false);
    }

    public LRUCache(final int cacheSize, boolean concurrent) {
        this.map = new LinkedHashMap<K, V>(cacheSize, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return cacheSize < super.size();
            }
        };
        if (concurrent) {
            this.map = Collections.synchronizedMap(this.map);
        }
    }

    public V get(K key) {
        return this.map.get(key);
    }

    public void put(K key, V value) {
        this.map.put(key, value);
    }

    public void clear() {
        this.map.clear();
    }

    public int size() {
        return this.map.size();
    }
}

