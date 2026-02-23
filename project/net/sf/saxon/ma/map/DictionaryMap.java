/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class DictionaryMap
implements MapItem {
    private HashMap<String, GroundedValue> hashMap = new HashMap();

    public void initialPut(String key, GroundedValue value) {
        this.hashMap.put(key, value);
    }

    public void initialAppend(String key, GroundedValue value) {
        GroundedValue existingValue = this.hashMap.get(key);
        if (existingValue == null) {
            this.initialPut(key, value);
        } else {
            this.hashMap.put(key, existingValue.concatenate(value));
        }
    }

    @Override
    public GroundedValue get(AtomicValue key) {
        if (key instanceof StringValue) {
            return this.hashMap.get(key.getStringValue());
        }
        return null;
    }

    @Override
    public int size() {
        return this.hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.hashMap.isEmpty();
    }

    public AtomicIterator<StringValue> keys() {
        Iterator<String> base = this.hashMap.keySet().iterator();
        return () -> base.hasNext() ? new StringValue((CharSequence)base.next()) : null;
    }

    @Override
    public Iterable<KeyValuePair> keyValuePairs() {
        ArrayList<KeyValuePair> pairs = new ArrayList<KeyValuePair>();
        this.hashMap.forEach((k, v) -> pairs.add(new KeyValuePair(new StringValue((CharSequence)k), (GroundedValue)v)));
        return pairs;
    }

    @Override
    public MapItem addEntry(AtomicValue key, GroundedValue value) {
        return this.toHashTrieMap().addEntry(key, value);
    }

    @Override
    public MapItem remove(AtomicValue key) {
        return this.toHashTrieMap().remove(key);
    }

    @Override
    public boolean conforms(AtomicType keyType, SequenceType valueType, TypeHierarchy th) {
        if (this.isEmpty()) {
            return true;
        }
        if (keyType != BuiltInAtomicType.STRING && keyType != BuiltInAtomicType.ANY_ATOMIC) {
            return false;
        }
        if (valueType.equals(SequenceType.ANY_SEQUENCE)) {
            return true;
        }
        for (GroundedValue val : this.hashMap.values()) {
            try {
                if (valueType.matches(val, th)) continue;
                return false;
            } catch (XPathException e) {
                throw new AssertionError((Object)e);
            }
        }
        return true;
    }

    @Override
    public ItemType getItemType(TypeHierarchy th) {
        ItemType valueType = null;
        int valueCard = 0;
        AtomicIterator<StringValue> keyIter = this.keys();
        for (Map.Entry<String, GroundedValue> entry : this.hashMap.entrySet()) {
            GroundedValue val = entry.getValue();
            if (valueType == null) {
                valueType = SequenceTool.getItemType(val, th);
                valueCard = SequenceTool.getCardinality(val);
                continue;
            }
            valueType = Type.getCommonSuperType(valueType, SequenceTool.getItemType(val, th), th);
            valueCard = Cardinality.union(valueCard, SequenceTool.getCardinality(val));
        }
        if (valueType == null) {
            return MapType.EMPTY_MAP_TYPE;
        }
        return new MapType(BuiltInAtomicType.STRING, SequenceType.makeSequenceType(valueType, valueCard));
    }

    @Override
    public UType getKeyUType() {
        return this.hashMap.isEmpty() ? UType.VOID : UType.STRING;
    }

    private HashTrieMap toHashTrieMap() {
        HashTrieMap target = new HashTrieMap();
        this.hashMap.forEach((k, v) -> target.initialPut(new StringValue((CharSequence)k), (GroundedValue)v));
        return target;
    }
}

