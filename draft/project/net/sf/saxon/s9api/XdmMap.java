/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmFunctionItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.jiter.MappingJavaIterator;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.StringValue;

public class XdmMap
extends XdmFunctionItem {
    public XdmMap() {
        this.setValue(new HashTrieMap());
    }

    public XdmMap(MapItem map) {
        this.setValue(map);
    }

    public XdmMap(Map<? extends XdmAtomicValue, ? extends XdmValue> map) {
        HashTrieMap val = new HashTrieMap();
        for (Map.Entry<? extends XdmAtomicValue, ? extends XdmValue> entry : map.entrySet()) {
            val.initialPut(entry.getKey().getUnderlyingValue(), entry.getValue().getUnderlyingValue());
        }
        this.setValue(val);
    }

    @Override
    public MapItem getUnderlyingValue() {
        return (MapItem)super.getUnderlyingValue();
    }

    public int mapSize() {
        return this.getUnderlyingValue().size();
    }

    public XdmMap put(XdmAtomicValue key, XdmValue value) {
        XdmMap map2 = new XdmMap();
        map2.setValue(this.getUnderlyingValue().addEntry(key.getUnderlyingValue(), value.getUnderlyingValue()));
        return map2;
    }

    public XdmMap remove(XdmAtomicValue key) {
        XdmMap map2 = new XdmMap();
        map2.setValue(this.getUnderlyingValue().remove(key.getUnderlyingValue()));
        return map2;
    }

    public Set<XdmAtomicValue> keySet() {
        return new AbstractSet<XdmAtomicValue>(){

            @Override
            public Iterator<XdmAtomicValue> iterator() {
                return new MappingJavaIterator<KeyValuePair, XdmAtomicValue>(XdmMap.this.getUnderlyingValue().keyValuePairs().iterator(), kvp -> (XdmAtomicValue)XdmValue.wrap(kvp.key));
            }

            @Override
            public int size() {
                return XdmMap.this.getUnderlyingValue().size();
            }

            @Override
            public boolean contains(Object o) {
                return XdmMap.this.getUnderlyingValue().get(((XdmAtomicValue)o).getUnderlyingValue()) != null;
            }
        };
    }

    public Map<XdmAtomicValue, XdmValue> asImmutableMap() {
        final XdmMap base = this;
        return new AbstractMap<XdmAtomicValue, XdmValue>(){

            @Override
            public Set<Map.Entry<XdmAtomicValue, XdmValue>> entrySet() {
                return base.entrySet();
            }

            @Override
            public int size() {
                return base.mapSize();
            }

            @Override
            public boolean isEmpty() {
                return base.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return key instanceof XdmAtomicValue && base.containsKey((XdmAtomicValue)key);
            }

            @Override
            public XdmValue get(Object key) {
                return key instanceof XdmAtomicValue ? base.get((XdmAtomicValue)key) : null;
            }

            @Override
            public XdmValue put(XdmAtomicValue key, XdmValue value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public XdmValue remove(Object key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putAll(Map<? extends XdmAtomicValue, ? extends XdmValue> m) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<XdmAtomicValue> keySet() {
                return base.keySet();
            }

            @Override
            public Collection<XdmValue> values() {
                return base.values();
            }
        };
    }

    @Override
    public Map<XdmAtomicValue, XdmValue> asMap() {
        return new HashMap<XdmAtomicValue, XdmValue>(this.asImmutableMap());
    }

    public void clear() {
        throw new UnsupportedOperationException("XdmMap is immutable");
    }

    @Override
    public boolean isEmpty() {
        return this.getUnderlyingValue().isEmpty();
    }

    public boolean containsKey(XdmAtomicValue key) {
        return this.getUnderlyingValue().get(key.getUnderlyingValue()) != null;
    }

    public XdmValue get(XdmAtomicValue key) {
        if (key == null) {
            throw new NullPointerException();
        }
        GroundedValue v = this.getUnderlyingValue().get(key.getUnderlyingValue());
        return v == null ? null : XdmValue.wrap(v);
    }

    public XdmValue get(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        GroundedValue v = this.getUnderlyingValue().get(new StringValue(key));
        return v == null ? null : XdmValue.wrap(v);
    }

    public XdmValue get(long key) {
        GroundedValue v = this.getUnderlyingValue().get(new Int64Value(key));
        return v == null ? null : XdmValue.wrap(v);
    }

    public XdmValue get(double key) {
        GroundedValue v = this.getUnderlyingValue().get(new DoubleValue(key));
        return v == null ? null : XdmValue.wrap(v);
    }

    public Collection<XdmValue> values() {
        ArrayList<XdmValue> result = new ArrayList<XdmValue>();
        for (KeyValuePair keyValuePair : this.getUnderlyingValue().keyValuePairs()) {
            result.add(XdmValue.wrap(keyValuePair.value));
        }
        return result;
    }

    public Set<Map.Entry<XdmAtomicValue, XdmValue>> entrySet() {
        HashSet<Map.Entry<XdmAtomicValue, XdmValue>> result = new HashSet<Map.Entry<XdmAtomicValue, XdmValue>>();
        for (KeyValuePair keyValuePair : this.getUnderlyingValue().keyValuePairs()) {
            result.add(new XdmMapEntry(keyValuePair));
        }
        return result;
    }

    public static XdmMap makeMap(Map input) throws IllegalArgumentException {
        HashTrieMap result = new HashTrieMap();
        for (Map.Entry entry : input.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            XdmAtomicValue xKey = XdmAtomicValue.makeAtomicValue(key);
            XdmValue xValue = XdmValue.makeValue(value);
            result.initialPut(xKey.getUnderlyingValue(), xValue.getUnderlyingValue());
        }
        return new XdmMap(result);
    }

    private static class XdmMapEntry
    implements Map.Entry<XdmAtomicValue, XdmValue> {
        KeyValuePair pair;

        public XdmMapEntry(KeyValuePair pair) {
            this.pair = pair;
        }

        @Override
        public XdmAtomicValue getKey() {
            return (XdmAtomicValue)XdmValue.wrap(this.pair.key);
        }

        @Override
        public XdmValue getValue() {
            return XdmValue.wrap(this.pair.value);
        }

        @Override
        public XdmValue setValue(XdmValue value) {
            throw new UnsupportedOperationException();
        }
    }
}

