/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.Iterator;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.ma.trie.ImmutableHashTrieMap;
import net.sf.saxon.ma.trie.ImmutableMap;
import net.sf.saxon.ma.trie.Tuple2;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;

public class HashTrieMap
implements MapItem {
    private ImmutableMap<AtomicMatchKey, KeyValuePair> imap;
    private UType keyUType = UType.VOID;
    private UType valueUType = UType.VOID;
    private AtomicType keyAtomicType = ErrorType.getInstance();
    private ItemType valueItemType = ErrorType.getInstance();
    private int valueCardinality = 0;
    private int entries;

    public HashTrieMap() {
        this.imap = ImmutableHashTrieMap.empty();
        this.entries = 0;
    }

    public static HashTrieMap singleton(AtomicValue key, GroundedValue value) {
        return new HashTrieMap().addEntry(key, value);
    }

    public HashTrieMap(ImmutableMap<AtomicMatchKey, KeyValuePair> imap) {
        this.imap = imap;
        this.entries = -1;
    }

    public static HashTrieMap copy(MapItem map) {
        if (map instanceof HashTrieMap) {
            return (HashTrieMap)map;
        }
        HashTrieMap m2 = new HashTrieMap();
        for (KeyValuePair pair : map.keyValuePairs()) {
            m2 = m2.addEntry(pair.key, pair.value);
        }
        return m2;
    }

    private void updateTypeInformation(AtomicValue key, Sequence val, boolean wasEmpty) {
        if (wasEmpty) {
            this.keyUType = key.getUType();
            this.valueUType = SequenceTool.getUType(val);
            this.keyAtomicType = key.getItemType();
            this.valueItemType = MapItem.getItemTypeOfSequence(val);
            this.valueCardinality = SequenceTool.getCardinality(val);
        } else {
            this.keyUType = this.keyUType.union(key.getUType());
            this.valueUType = this.valueUType.union(SequenceTool.getUType(val));
            this.valueCardinality = Cardinality.union(this.valueCardinality, SequenceTool.getCardinality(val));
            if (key.getItemType() != this.keyAtomicType) {
                this.keyAtomicType = null;
            }
            if (!MapItem.isKnownToConform(val, this.valueItemType)) {
                this.valueItemType = null;
            }
        }
    }

    @Override
    public int size() {
        if (this.entries >= 0) {
            return this.entries;
        }
        int count = 0;
        for (KeyValuePair entry : this.keyValuePairs()) {
            ++count;
        }
        this.entries = count;
        return this.entries;
    }

    @Override
    public boolean isEmpty() {
        return this.entries == 0 || !this.imap.iterator().hasNext();
    }

    @Override
    public boolean conforms(AtomicType requiredKeyType, SequenceType requiredValueType, TypeHierarchy th) {
        int requiredValueCard;
        ItemType upperBoundValueType;
        Affinity rel;
        ItemType requiredValueItemType;
        ItemType upperBoundKeyType;
        Affinity rel2;
        if (this.isEmpty()) {
            return true;
        }
        if (this.keyAtomicType == requiredKeyType && this.valueItemType == requiredValueType.getPrimaryType() && Cardinality.subsumes(requiredValueType.getCardinality(), this.valueCardinality)) {
            return true;
        }
        boolean needFullCheck = false;
        if (requiredKeyType != BuiltInAtomicType.ANY_ATOMIC && (rel2 = th.relationship(requiredKeyType, upperBoundKeyType = this.keyUType.toItemType())) != Affinity.SAME_TYPE && rel2 != Affinity.SUBSUMES) {
            if (rel2 == Affinity.DISJOINT) {
                return false;
            }
            needFullCheck = true;
        }
        if ((requiredValueItemType = requiredValueType.getPrimaryType()) != BuiltInAtomicType.ANY_ATOMIC && (rel = th.relationship(requiredValueItemType, upperBoundValueType = this.valueUType.toItemType())) != Affinity.SAME_TYPE && rel != Affinity.SUBSUMES) {
            if (rel == Affinity.DISJOINT) {
                return false;
            }
            needFullCheck = true;
        }
        if (!Cardinality.subsumes(requiredValueCard = requiredValueType.getCardinality(), this.valueCardinality)) {
            needFullCheck = true;
        }
        if (needFullCheck) {
            Item key;
            AtomicIterator<? extends AtomicValue> keyIter = this.keys();
            while ((key = keyIter.next()) != null) {
                if (!requiredKeyType.matches(key, th)) {
                    return false;
                }
                GroundedValue val = this.get((AtomicValue)key);
                try {
                    if (requiredValueType.matches(val, th)) continue;
                    return false;
                } catch (XPathException e) {
                    throw new AssertionError((Object)e);
                }
            }
        }
        return true;
    }

    @Override
    public MapType getItemType(TypeHierarchy th) {
        Item key;
        ItemType keyType = null;
        ItemType valueType = null;
        int valueCard = 0;
        AtomicIterator<? extends AtomicValue> keyIter = this.keys();
        while ((key = keyIter.next()) != null) {
            GroundedValue val = this.get((AtomicValue)key);
            if (keyType == null) {
                keyType = ((AtomicValue)key).getItemType();
                valueType = SequenceTool.getItemType(val, th);
                valueCard = SequenceTool.getCardinality(val);
                continue;
            }
            keyType = (AtomicType)Type.getCommonSuperType(keyType, ((AtomicValue)key).getItemType(), th);
            valueType = Type.getCommonSuperType(valueType, SequenceTool.getItemType(val, th), th);
            valueCard = Cardinality.union(valueCard, SequenceTool.getCardinality(val));
        }
        if (keyType == null) {
            this.keyUType = UType.VOID;
            this.valueUType = UType.VOID;
            this.valueCardinality = 0;
            return MapType.ANY_MAP_TYPE;
        }
        this.keyUType = keyType.getUType();
        this.valueUType = valueType.getUType();
        this.valueCardinality = valueCard;
        return new MapType((AtomicType)keyType, SequenceType.makeSequenceType(valueType, valueCard));
    }

    @Override
    public UType getKeyUType() {
        return this.keyUType;
    }

    @Override
    public HashTrieMap addEntry(AtomicValue key, GroundedValue value) {
        AtomicMatchKey amk = this.makeKey(key);
        boolean isNew = this.imap.get(amk) == null;
        boolean empty = this.isEmpty();
        ImmutableMap<AtomicMatchKey, KeyValuePair> imap2 = this.imap.put(amk, new KeyValuePair(key, value));
        HashTrieMap t2 = new HashTrieMap(imap2);
        t2.valueCardinality = this.valueCardinality;
        t2.keyUType = this.keyUType;
        t2.valueUType = this.valueUType;
        t2.keyAtomicType = this.keyAtomicType;
        t2.valueItemType = this.valueItemType;
        t2.updateTypeInformation(key, value, empty);
        if (this.entries >= 0) {
            t2.entries = isNew ? this.entries + 1 : this.entries;
        }
        return t2;
    }

    public boolean initialPut(AtomicValue key, GroundedValue value) {
        boolean empty = this.isEmpty();
        boolean exists = this.get(key) != null;
        this.imap = this.imap.put(this.makeKey(key), new KeyValuePair(key, value));
        this.updateTypeInformation(key, value, empty);
        this.entries = -1;
        return exists;
    }

    private AtomicMatchKey makeKey(AtomicValue key) {
        return key.asMapKey();
    }

    @Override
    public HashTrieMap remove(AtomicValue key) {
        ImmutableMap<AtomicMatchKey, KeyValuePair> m2 = this.imap.remove(this.makeKey(key));
        if (m2 == this.imap) {
            return this;
        }
        HashTrieMap result = new HashTrieMap(m2);
        result.keyUType = this.keyUType;
        result.valueUType = this.valueUType;
        result.valueCardinality = this.valueCardinality;
        result.entries = this.entries - 1;
        return result;
    }

    @Override
    public GroundedValue get(AtomicValue key) {
        KeyValuePair o = this.imap.get(this.makeKey(key));
        return o == null ? null : o.value;
    }

    public KeyValuePair getKeyValuePair(AtomicValue key) {
        return this.imap.get(this.makeKey(key));
    }

    @Override
    public AtomicIterator<? extends AtomicValue> keys() {
        return new AtomicIterator<AtomicValue>(){
            Iterator<Tuple2<AtomicMatchKey, KeyValuePair>> base;
            {
                this.base = HashTrieMap.this.imap.iterator();
            }

            @Override
            public AtomicValue next() {
                if (this.base.hasNext()) {
                    return ((KeyValuePair)this.base.next()._2).key;
                }
                return null;
            }
        };
    }

    @Override
    public Iterable<KeyValuePair> keyValuePairs() {
        return new Iterable<KeyValuePair>(){

            @Override
            public Iterator<KeyValuePair> iterator() {
                return new Iterator<KeyValuePair>(){
                    Iterator<Tuple2<AtomicMatchKey, KeyValuePair>> base;
                    {
                        this.base = HashTrieMap.this.imap.iterator();
                    }

                    @Override
                    public boolean hasNext() {
                        return this.base.hasNext();
                    }

                    @Override
                    public KeyValuePair next() {
                        return (KeyValuePair)this.base.next()._2;
                    }

                    @Override
                    public void remove() {
                        this.base.remove();
                    }
                };
            }
        };
    }

    public void diagnosticDump() {
        System.err.println("Map details:");
        for (Tuple2<AtomicMatchKey, KeyValuePair> tuple2 : this.imap) {
            AtomicMatchKey k1 = (AtomicMatchKey)tuple2._1;
            AtomicValue k2 = ((KeyValuePair)tuple2._2).key;
            GroundedValue v = ((KeyValuePair)tuple2._2).value;
            System.err.println(k1.getClass() + " " + k1 + " #:" + k1.hashCode() + " = (" + k2.getClass() + " " + k2 + " : " + v + ")");
        }
    }

    public String toString() {
        return MapItem.mapToString(this);
    }
}

