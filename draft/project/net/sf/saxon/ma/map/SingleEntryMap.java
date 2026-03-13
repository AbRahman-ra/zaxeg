/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.Iterator;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.iter.SingleAtomicIterator;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.SequenceType;

public class SingleEntryMap
implements MapItem {
    public AtomicValue key;
    public GroundedValue value;

    public SingleEntryMap(AtomicValue key, GroundedValue value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public GroundedValue get(AtomicValue key) {
        return this.key.asMapKey().equals(key.asMapKey()) ? this.value : null;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public AtomicIterator keys() {
        return new SingleAtomicIterator<AtomicValue>(this.key);
    }

    @Override
    public Iterable<KeyValuePair> keyValuePairs() {
        return new Iterable<KeyValuePair>(){

            @Override
            public Iterator<KeyValuePair> iterator() {
                return new MonoIterator<KeyValuePair>(new KeyValuePair(SingleEntryMap.this.key, SingleEntryMap.this.value));
            }
        };
    }

    @Override
    public MapItem addEntry(AtomicValue key, GroundedValue value) {
        return this.toHashTrieMap().addEntry(key, value);
    }

    @Override
    public MapItem remove(AtomicValue key) {
        return this.get(key) == null ? this : new HashTrieMap();
    }

    @Override
    public boolean conforms(AtomicType keyType, SequenceType valueType, TypeHierarchy th) {
        try {
            return keyType.matches(this.key, th) && valueType.matches(this.value, th);
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public ItemType getItemType(TypeHierarchy th) {
        return new MapType(this.key.getItemType(), SequenceType.makeSequenceType(SequenceTool.getItemType(this.value, th), SequenceTool.getCardinality(this.value)));
    }

    @Override
    public UType getKeyUType() {
        return this.key.getUType();
    }

    private HashTrieMap toHashTrieMap() {
        HashTrieMap target = new HashTrieMap();
        target.initialPut(this.key, this.value);
        return target;
    }
}

