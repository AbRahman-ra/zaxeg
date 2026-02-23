/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.tree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

class ConcurrentReaderHashMap
extends AbstractMap
implements Map,
Cloneable,
Serializable {
    protected final BarrierLock barrierLock = new BarrierLock();
    protected transient Object lastWrite;
    public static int DEFAULT_INITIAL_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 0x40000000;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected transient Entry[] table;
    protected transient int count;
    protected int threshold;
    protected float loadFactor;
    protected transient Set keySet = null;
    protected transient Set entrySet = null;
    protected transient Collection values = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void recordModification(Object x) {
        BarrierLock barrierLock = this.barrierLock;
        synchronized (barrierLock) {
            this.lastWrite = x;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final Entry[] getTableForReading() {
        BarrierLock barrierLock = this.barrierLock;
        synchronized (barrierLock) {
            return this.table;
        }
    }

    private int p2capacity(int initialCapacity) {
        int result;
        int cap = initialCapacity;
        if (cap > 0x40000000 || cap < 0) {
            result = 0x40000000;
        } else {
            for (result = 4; result < cap; result <<= 1) {
            }
        }
        return result;
    }

    private static int hash(Object x) {
        int h = x.hashCode();
        return (h << 7) - h + (h >>> 9) + (h >>> 17);
    }

    protected boolean eq(Object x, Object y) {
        return x == y || x.equals(y);
    }

    public ConcurrentReaderHashMap(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0.0f) {
            throw new IllegalArgumentException("Illegal Load factor: " + loadFactor);
        }
        this.loadFactor = loadFactor;
        int cap = this.p2capacity(initialCapacity);
        this.table = new Entry[cap];
        this.threshold = (int)((float)cap * loadFactor);
    }

    public ConcurrentReaderHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public ConcurrentReaderHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, 0.75f);
    }

    public ConcurrentReaderHashMap(Map t) {
        this(Math.max((int)((float)t.size() / 0.75f) + 1, 16), 0.75f);
        this.putAll(t);
    }

    public synchronized int size() {
        return this.count;
    }

    public synchronized boolean isEmpty() {
        return this.count == 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object get(Object key) {
        Entry first;
        int hash = ConcurrentReaderHashMap.hash(key);
        Entry[] tab = this.table;
        int index = hash & tab.length - 1;
        Entry e = first = tab[index];
        while (true) {
            if (e == null) {
                Entry[] reread = this.getTableForReading();
                if (tab == reread && first == tab[index]) {
                    return null;
                }
                tab = reread;
                index = hash & tab.length - 1;
                e = first = tab[index];
                continue;
            }
            if (e.hash == hash && this.eq(key, e.key)) {
                Object value = e.value;
                if (value != null) {
                    return value;
                }
                ConcurrentReaderHashMap concurrentReaderHashMap = this;
                synchronized (concurrentReaderHashMap) {
                    tab = this.table;
                }
                index = hash & tab.length - 1;
                e = first = tab[index];
                continue;
            }
            e = e.next;
        }
    }

    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object put(Object key, Object value) {
        Entry first;
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = ConcurrentReaderHashMap.hash(key);
        Entry[] tab = this.table;
        int index = hash & tab.length - 1;
        Entry e = first = tab[index];
        while (!(e == null || e.hash == hash && this.eq(key, e.key))) {
            e = e.next;
        }
        ConcurrentReaderHashMap concurrentReaderHashMap = this;
        synchronized (concurrentReaderHashMap) {
            if (tab == this.table) {
                if (e == null) {
                    if (first == tab[index]) {
                        Entry newEntry;
                        tab[index] = newEntry = new Entry(hash, key, value, first);
                        if (++this.count >= this.threshold) {
                            this.rehash();
                        } else {
                            this.recordModification(newEntry);
                        }
                        return null;
                    }
                } else {
                    Object oldValue = e.value;
                    if (first == tab[index] && oldValue != null) {
                        e.value = value;
                        return oldValue;
                    }
                }
            }
            return this.sput(key, value, hash);
        }
    }

    protected Object sput(Object key, Object value, int hash) {
        Entry first;
        Entry[] tab = this.table;
        int index = hash & tab.length - 1;
        Entry e = first = tab[index];
        while (true) {
            if (e == null) {
                Entry newEntry;
                tab[index] = newEntry = new Entry(hash, key, value, first);
                if (++this.count >= this.threshold) {
                    this.rehash();
                } else {
                    this.recordModification(newEntry);
                }
                return null;
            }
            if (e.hash == hash && this.eq(key, e.key)) {
                Object oldValue = e.value;
                e.value = value;
                return oldValue;
            }
            e = e.next;
        }
    }

    protected void rehash() {
        Entry[] oldTable = this.table;
        int oldCapacity = oldTable.length;
        if (oldCapacity >= 0x40000000) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }
        int newCapacity = oldCapacity << 1;
        int mask = newCapacity - 1;
        this.threshold = (int)((float)newCapacity * this.loadFactor);
        Entry[] newTable = new Entry[newCapacity];
        for (int i = 0; i < oldCapacity; ++i) {
            int k;
            Entry e = oldTable[i];
            if (e == null) continue;
            int idx = e.hash & mask;
            Entry next = e.next;
            if (next == null) {
                newTable[idx] = e;
                continue;
            }
            Entry lastRun = e;
            int lastIdx = idx;
            Entry last = next;
            while (last != null) {
                k = last.hash & mask;
                if (k != lastIdx) {
                    lastIdx = k;
                    lastRun = last;
                }
                last = last.next;
            }
            newTable[lastIdx] = lastRun;
            Entry p = e;
            while (p != lastRun) {
                k = p.hash & mask;
                newTable[k] = new Entry(p.hash, p.key, p.value, newTable[k]);
                p = p.next;
            }
        }
        this.table = newTable;
        this.recordModification(newTable);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object remove(Object key) {
        Entry first;
        int hash = ConcurrentReaderHashMap.hash(key);
        Entry[] tab = this.table;
        int index = hash & tab.length - 1;
        Entry e = first = tab[index];
        e = first;
        while (!(e == null || e.hash == hash && this.eq(key, e.key))) {
            e = e.next;
        }
        ConcurrentReaderHashMap concurrentReaderHashMap = this;
        synchronized (concurrentReaderHashMap) {
            if (tab == this.table) {
                if (e == null) {
                    if (first == tab[index]) {
                        return null;
                    }
                } else {
                    Object oldValue = e.value;
                    if (first == tab[index] && oldValue != null) {
                        e.value = null;
                        --this.count;
                        Entry head = e.next;
                        Entry p = first;
                        while (p != e) {
                            head = new Entry(p.hash, p.key, p.value, head);
                            p = p.next;
                        }
                        tab[index] = head;
                        this.recordModification(head);
                        return oldValue;
                    }
                }
            }
            return this.sremove(key, hash);
        }
    }

    protected Object sremove(Object key, int hash) {
        Entry first;
        Entry[] tab = this.table;
        int index = hash & tab.length - 1;
        Entry e = first = tab[index];
        while (e != null) {
            if (e.hash == hash && this.eq(key, e.key)) {
                Object oldValue = e.value;
                e.value = null;
                --this.count;
                Entry head = e.next;
                Entry p = first;
                while (p != e) {
                    head = new Entry(p.hash, p.key, p.value, head);
                    p = p.next;
                }
                tab[index] = head;
                this.recordModification(head);
                return oldValue;
            }
            e = e.next;
        }
        return null;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Entry[] tab = this.getTableForReading();
        for (int i = 0; i < tab.length; ++i) {
            Entry e = tab[i];
            while (e != null) {
                if (value.equals(e.value)) {
                    return true;
                }
                e = e.next;
            }
        }
        return false;
    }

    public boolean contains(Object value) {
        return this.containsValue(value);
    }

    public synchronized void putAll(Map t) {
        int n = t.size();
        if (n == 0) {
            return;
        }
        while (n >= this.threshold) {
            this.rehash();
        }
        Iterator it = t.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            this.put(key, value);
        }
    }

    public synchronized void clear() {
        Entry[] tab = this.table;
        for (int i = 0; i < tab.length; ++i) {
            Entry e = tab[i];
            while (e != null) {
                e.value = null;
                e = e.next;
            }
            tab[i] = null;
        }
        this.count = 0;
        this.recordModification(tab);
    }

    public synchronized Object clone() {
        try {
            ConcurrentReaderHashMap t = (ConcurrentReaderHashMap)super.clone();
            t.keySet = null;
            t.entrySet = null;
            t.values = null;
            Entry[] tab = this.table;
            Entry[] ttab = t.table = new Entry[tab.length];
            for (int i = 0; i < tab.length; ++i) {
                Entry first = null;
                Entry e = tab[i];
                while (e != null) {
                    first = new Entry(e.hash, e.key, e.value, first);
                    e = e.next;
                }
                ttab[i] = first;
            }
            return t;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public Set keySet() {
        Set ks = this.keySet;
        return ks != null ? ks : (this.keySet = new KeySet());
    }

    public Collection values() {
        Collection vs = this.values;
        return vs != null ? vs : (this.values = new Values());
    }

    public Set entrySet() {
        Set es = this.entrySet;
        return es != null ? es : (this.entrySet = new EntrySet());
    }

    protected synchronized boolean findAndRemoveEntry(Map.Entry entry) {
        Object key = entry.getKey();
        Object v = this.get(key);
        if (v != null && v.equals(entry.getValue())) {
            this.remove(key);
            return true;
        }
        return false;
    }

    public Enumeration keys() {
        return new KeyIterator();
    }

    public Enumeration elements() {
        return new ValueIterator();
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.table.length);
        s.writeInt(this.count);
        for (int index = this.table.length - 1; index >= 0; --index) {
            Entry entry = this.table[index];
            while (entry != null) {
                s.writeObject(entry.key);
                s.writeObject(entry.value);
                entry = entry.next;
            }
        }
    }

    private synchronized void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int numBuckets = s.readInt();
        this.table = new Entry[numBuckets];
        int size = s.readInt();
        for (int i = 0; i < size; ++i) {
            Object key = s.readObject();
            Object value = s.readObject();
            this.put(key, value);
        }
    }

    public synchronized int capacity() {
        return this.table.length;
    }

    public float loadFactor() {
        return this.loadFactor;
    }

    protected class ValueIterator
    extends HashIterator {
        protected ValueIterator() {
        }

        protected Object returnValueOfNext() {
            return this.currentValue;
        }
    }

    protected class KeyIterator
    extends HashIterator {
        protected KeyIterator() {
        }

        protected Object returnValueOfNext() {
            return this.currentKey;
        }
    }

    protected class HashIterator
    implements Iterator,
    Enumeration {
        protected final Entry[] tab;
        protected int index;
        protected Entry entry = null;
        protected Object currentKey;
        protected Object currentValue;
        protected Entry lastReturned = null;

        protected HashIterator() {
            this.tab = ConcurrentReaderHashMap.this.getTableForReading();
            this.index = this.tab.length - 1;
        }

        public boolean hasMoreElements() {
            return this.hasNext();
        }

        public Object nextElement() {
            return this.next();
        }

        public boolean hasNext() {
            do {
                if (this.entry != null) {
                    Object v = this.entry.value;
                    if (v != null) {
                        this.currentKey = this.entry.key;
                        this.currentValue = v;
                        return true;
                    }
                    this.entry = this.entry.next;
                }
                while (this.entry == null && this.index >= 0) {
                    this.entry = this.tab[this.index--];
                }
            } while (this.entry != null);
            this.currentValue = null;
            this.currentKey = null;
            return false;
        }

        protected Object returnValueOfNext() {
            return this.entry;
        }

        public Object next() {
            if (this.currentKey == null && !this.hasNext()) {
                throw new NoSuchElementException();
            }
            Object result = this.returnValueOfNext();
            this.lastReturned = this.entry;
            this.currentValue = null;
            this.currentKey = null;
            this.entry = this.entry.next;
            return result;
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }
            ConcurrentReaderHashMap.this.remove(this.lastReturned.key);
            this.lastReturned = null;
        }
    }

    protected static class Entry
    implements Map.Entry {
        protected final int hash;
        protected final Object key;
        protected final Entry next;
        protected volatile Object value;

        Entry(int hash, Object key, Object value, Entry next) {
            this.hash = hash;
            this.key = key;
            this.next = next;
            this.value = value;
        }

        public Object getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }

        public Object setValue(Object value) {
            if (value == null) {
                throw new NullPointerException();
            }
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return this.key.equals(e.getKey()) && this.value.equals(e.getValue());
        }

        public int hashCode() {
            return this.key.hashCode() ^ this.value.hashCode();
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    private class EntrySet
    extends AbstractSet {
        private EntrySet() {
        }

        public Iterator iterator() {
            return new HashIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            Object v = ConcurrentReaderHashMap.this.get(entry.getKey());
            return v != null && v.equals(entry.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            return ConcurrentReaderHashMap.this.findAndRemoveEntry((Map.Entry)o);
        }

        public int size() {
            return ConcurrentReaderHashMap.this.size();
        }

        public void clear() {
            ConcurrentReaderHashMap.this.clear();
        }
    }

    private class Values
    extends AbstractCollection {
        private Values() {
        }

        public Iterator iterator() {
            return new ValueIterator();
        }

        public int size() {
            return ConcurrentReaderHashMap.this.size();
        }

        public boolean contains(Object o) {
            return ConcurrentReaderHashMap.this.containsValue(o);
        }

        public void clear() {
            ConcurrentReaderHashMap.this.clear();
        }
    }

    private class KeySet
    extends AbstractSet {
        private KeySet() {
        }

        public Iterator iterator() {
            return new KeyIterator();
        }

        public int size() {
            return ConcurrentReaderHashMap.this.size();
        }

        public boolean contains(Object o) {
            return ConcurrentReaderHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return ConcurrentReaderHashMap.this.remove(o) != null;
        }

        public void clear() {
            ConcurrentReaderHashMap.this.clear();
        }
    }

    protected static class BarrierLock
    implements Serializable {
        protected BarrierLock() {
        }
    }
}

