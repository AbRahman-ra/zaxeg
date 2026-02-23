/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.trie;

import java.util.Iterator;
import net.sf.saxon.ma.trie.Tuple2;

public interface ImmutableMap<K, V>
extends Iterable<Tuple2<K, V>> {
    public ImmutableMap<K, V> put(K var1, V var2);

    public ImmutableMap<K, V> remove(K var1);

    public V get(K var1);

    @Override
    public Iterator<Tuple2<K, V>> iterator();
}

