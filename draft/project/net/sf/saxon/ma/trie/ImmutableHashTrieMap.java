/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.trie;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import net.sf.saxon.ma.trie.ImmutableList;
import net.sf.saxon.ma.trie.ImmutableMap;
import net.sf.saxon.ma.trie.Tuple2;

public abstract class ImmutableHashTrieMap<K, V>
implements ImmutableMap<K, V>,
Iterable<Tuple2<K, V>> {
    private static final ImmutableHashTrieMap EMPTY_NODE = new EmptyHashNode();
    private static final int BITS = 5;
    private static final int FANOUT = 32;
    private static final int MASK = 31;

    public static <K, V> ImmutableHashTrieMap<K, V> empty() {
        return EMPTY_NODE;
    }

    private static <K> int getBucket(int shift, K key) {
        return key.hashCode() >> shift & 0x1F;
    }

    @Override
    public ImmutableHashTrieMap<K, V> put(K key, V value) {
        return this.put(0, key, value);
    }

    @Override
    public ImmutableHashTrieMap<K, V> remove(K key) {
        return this.remove(0, key);
    }

    @Override
    public V get(K key) {
        return this.get(0, key);
    }

    abstract ImmutableHashTrieMap<K, V> put(int var1, K var2, V var3);

    abstract ImmutableHashTrieMap<K, V> remove(int var1, K var2);

    abstract V get(int var1, K var2);

    abstract boolean isArrayNode();

    private static <K, V> ImmutableHashTrieMap<K, V> newArrayHashNode(int shift, int hash1, ImmutableHashTrieMap<K, V> subNode1, int hash2, ImmutableHashTrieMap<K, V> subNode2) {
        int curShift = shift;
        int h1 = hash1 >> shift & 0x1F;
        int h2 = hash2 >> shift & 0x1F;
        LinkedList<Integer> buckets = new LinkedList<Integer>();
        while (h1 == h2) {
            buckets.add(0, h1);
            h1 = hash1 >> (curShift += 5) & 0x1F;
            h2 = hash2 >> curShift & 0x1F;
        }
        ArrayHashNode newNode = new BranchedArrayHashNode<K, V>(h1, subNode1, h2, subNode2);
        for (Integer bucket : buckets) {
            newNode = new SingletonArrayHashNode(bucket, newNode);
        }
        return newNode;
    }

    private static class SingletonArrayHashNode<K, V>
    extends ArrayHashNode<K, V> {
        private final int bucket;
        private final ImmutableHashTrieMap<K, V> subnode;

        private SingletonArrayHashNode(int bucket, ImmutableHashTrieMap<K, V> subnode) {
            assert (subnode instanceof ArrayHashNode);
            this.bucket = bucket;
            this.subnode = subnode;
        }

        @Override
        ImmutableHashTrieMap<K, V> put(int shift, K key, V value) {
            int bucket = ImmutableHashTrieMap.getBucket(shift, key);
            if (bucket == this.bucket) {
                return new SingletonArrayHashNode<K, V>(bucket, this.subnode.put(shift + 5, key, value));
            }
            return new BranchedArrayHashNode<K, V>(this.bucket, this.subnode, bucket, new EntryHashNode(key, value));
        }

        @Override
        ImmutableHashTrieMap<K, V> remove(int shift, K key) {
            int bucket = ImmutableHashTrieMap.getBucket(shift, key);
            if (bucket == this.bucket) {
                ImmutableHashTrieMap<K, V> newNode = this.subnode.remove(shift + 5, key);
                if (!newNode.isArrayNode()) {
                    return newNode;
                }
                return new SingletonArrayHashNode<K, V>(bucket, newNode);
            }
            return this;
        }

        @Override
        V get(int shift, K key) {
            int bucket = ImmutableHashTrieMap.getBucket(shift, key);
            if (bucket == this.bucket) {
                return this.subnode.get(shift + 5, key);
            }
            return null;
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return this.subnode.iterator();
        }
    }

    private static class BranchedArrayHashNode<K, V>
    extends ArrayHashNode<K, V> {
        private final ImmutableHashTrieMap<K, V>[] subnodes;
        private final int size;

        public BranchedArrayHashNode(int h1, ImmutableHashTrieMap<K, V> subNode1, int h2, ImmutableHashTrieMap<K, V> subNode2) {
            assert (h1 != h2);
            this.size = 2;
            this.subnodes = new ImmutableHashTrieMap[32];
            for (int i = 0; i < 32; ++i) {
                this.subnodes[i] = i == h1 ? subNode1 : (i == h2 ? subNode2 : EMPTY_NODE);
            }
        }

        public BranchedArrayHashNode(int size, ImmutableHashTrieMap<K, V>[] subnodes) {
            assert (subnodes.length == 32);
            this.size = size;
            this.subnodes = subnodes;
        }

        @Override
        ImmutableHashTrieMap<K, V> put(int shift, K key, V value) {
            int bucket = ImmutableHashTrieMap.getBucket(shift, key);
            ImmutableHashTrieMap[] newNodes = new ImmutableHashTrieMap[32];
            System.arraycopy(this.subnodes, 0, newNodes, 0, 32);
            int newSize = newNodes[bucket] == EMPTY_NODE ? this.size + 1 : this.size;
            newNodes[bucket] = newNodes[bucket].put(shift + 5, key, value);
            return new BranchedArrayHashNode<K, V>(newSize, newNodes);
        }

        @Override
        ImmutableHashTrieMap<K, V> remove(int shift, K key) {
            int newSize;
            int bucket = ImmutableHashTrieMap.getBucket(shift, key);
            if (this.subnodes[bucket] == EMPTY_NODE) {
                return this;
            }
            ImmutableHashTrieMap[] newNodes = new ImmutableHashTrieMap[32];
            System.arraycopy(this.subnodes, 0, newNodes, 0, 32);
            newNodes[bucket] = newNodes[bucket].remove(shift + 5, key);
            int n = newSize = newNodes[bucket] == EMPTY_NODE ? this.size - 1 : this.size;
            if (newSize == 1) {
                ImmutableHashTrieMap<K, V> orphanedEntry;
                int orphanedBucket = -1;
                for (int i = 0; i < 32; ++i) {
                    if (newNodes[i] == EMPTY_NODE) continue;
                    orphanedBucket = i;
                    break;
                }
                if ((orphanedEntry = this.subnodes[orphanedBucket]).isArrayNode()) {
                    return new SingletonArrayHashNode(orphanedBucket, orphanedEntry);
                }
                return orphanedEntry;
            }
            return new BranchedArrayHashNode<K, V>(newSize, newNodes);
        }

        @Override
        V get(int shift, K key) {
            int bucket = ImmutableHashTrieMap.getBucket(shift, key);
            return this.subnodes[bucket].get(shift + 5, key);
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return new Iterator<Tuple2<K, V>>(){
                private int bucket = 0;
                private Iterator<Tuple2<K, V>> childIterator = BranchedArrayHashNode.access$800(this)[0].iterator();

                @Override
                public boolean hasNext() {
                    if (this.childIterator.hasNext()) {
                        return true;
                    }
                    ++this.bucket;
                    while (this.bucket < 32) {
                        this.childIterator = subnodes[this.bucket].iterator();
                        if (this.childIterator.hasNext()) {
                            return true;
                        }
                        ++this.bucket;
                    }
                    return false;
                }

                @Override
                public Tuple2<K, V> next() {
                    return this.childIterator.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static abstract class ArrayHashNode<K, V>
    extends ImmutableHashTrieMap<K, V> {
        private ArrayHashNode() {
        }

        @Override
        boolean isArrayNode() {
            return true;
        }
    }

    private static class ListHashNode<K, V>
    extends ImmutableHashTrieMap<K, V> {
        private final ImmutableList<Tuple2<K, V>> entries;

        public ListHashNode(Tuple2<K, V> entry1, Tuple2<K, V> entry2) {
            assert (entry1._1.hashCode() == entry2._1.hashCode());
            this.entries = ImmutableList.empty().prepend(entry1).prepend(entry2);
        }

        private ListHashNode(ImmutableList<Tuple2<K, V>> entries) {
            assert (!entries.isEmpty());
            assert (!entries.tail().isEmpty());
            this.entries = entries;
        }

        @Override
        ImmutableHashTrieMap<K, V> put(int shift, K key, V value) {
            if (this.entries.head()._1.hashCode() != key.hashCode()) {
                return ImmutableHashTrieMap.newArrayHashNode(shift, this.entries.head()._1.hashCode(), (ImmutableHashTrieMap)this, key.hashCode(), (ImmutableHashTrieMap)new EntryHashNode(key, value));
            }
            ImmutableList<Tuple2<K, V>> newList = ImmutableList.empty();
            boolean found = false;
            for (Tuple2<K, V> entry : this.entries) {
                if (entry._1.equals(key)) {
                    newList = newList.prepend(new Tuple2<K, V>(key, value));
                    found = true;
                    continue;
                }
                newList = newList.prepend(entry);
            }
            if (!found) {
                newList = newList.prepend(new Tuple2<K, V>(key, value));
            }
            return new ListHashNode<K, V>(newList);
        }

        @Override
        ImmutableHashTrieMap<K, V> remove(int shift, K key) {
            ImmutableList<Tuple2<K, V>> newList = ImmutableList.empty();
            int size = 0;
            for (Tuple2<K, V> entry : this.entries) {
                if (entry._1.equals(key)) continue;
                newList = newList.prepend(entry);
                ++size;
            }
            if (size == 1) {
                Tuple2 entry = (Tuple2)newList.head();
                return new EntryHashNode(entry._1, entry._2);
            }
            return new ListHashNode<K, V>(newList);
        }

        @Override
        boolean isArrayNode() {
            return false;
        }

        @Override
        V get(int shift, K key) {
            for (Tuple2<K, V> entry : this.entries) {
                if (!entry._1.equals(key)) continue;
                return (V)entry._2;
            }
            return null;
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return new Iterator<Tuple2<K, V>>(){
                private ImmutableList<Tuple2<K, V>> curList;
                {
                    this.curList = entries;
                }

                @Override
                public boolean hasNext() {
                    return !this.curList.isEmpty();
                }

                @Override
                public Tuple2<K, V> next() {
                    Tuple2 retVal = this.curList.head();
                    this.curList = this.curList.tail();
                    return retVal;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static class EntryHashNode<K, V>
    extends ImmutableHashTrieMap<K, V> {
        private final K key;
        private final V value;

        private EntryHashNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        ImmutableHashTrieMap<K, V> put(int shift, K key, V value) {
            if (this.key.equals(key)) {
                return new EntryHashNode<K, V>(key, value);
            }
            if (this.key.hashCode() == key.hashCode()) {
                return new ListHashNode<K, V>(new Tuple2<K, V>(this.key, this.value), new Tuple2<K, V>(key, value));
            }
            return ImmutableHashTrieMap.newArrayHashNode(shift, this.key.hashCode(), (ImmutableHashTrieMap)this, key.hashCode(), (ImmutableHashTrieMap)new EntryHashNode<K, V>(key, value));
        }

        @Override
        ImmutableHashTrieMap<K, V> remove(int shift, K key) {
            if (this.key.equals(key)) {
                return EntryHashNode.empty();
            }
            return this;
        }

        @Override
        boolean isArrayNode() {
            return false;
        }

        @Override
        V get(int shift, K key) {
            if (this.key.equals(key)) {
                return this.value;
            }
            return null;
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return Collections.singleton(new Tuple2<K, V>(this.key, this.value)).iterator();
        }
    }

    private static class EmptyHashNode<K, V>
    extends ImmutableHashTrieMap<K, V> {
        private EmptyHashNode() {
        }

        @Override
        ImmutableHashTrieMap<K, V> put(int shift, K key, V value) {
            return new EntryHashNode(key, value);
        }

        @Override
        ImmutableHashTrieMap<K, V> remove(int shift, K key) {
            return this;
        }

        @Override
        boolean isArrayNode() {
            return false;
        }

        @Override
        V get(int shift, K key) {
            return null;
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return Collections.emptySet().iterator();
        }
    }
}

