/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.functions.Count;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class RangeKey
implements MapItem {
    private UnicodeString min;
    private UnicodeString max;
    private TreeMap<AtomicMatchKey, Object> index;

    public RangeKey(String min, String max, TreeMap<AtomicMatchKey, Object> index) {
        this.min = min == null ? null : UnicodeString.makeUnicodeString(min);
        this.max = max == null ? null : UnicodeString.makeUnicodeString(max);
        this.index = index;
    }

    @Override
    public GroundedValue get(AtomicValue key) {
        UnicodeString k = UnicodeString.makeUnicodeString(key.getStringValueCS());
        if (!(this.min != null && this.min.compareTo(k) > 0 || this.max != null && this.max.compareTo(k) < 0)) {
            Object value = this.index.get(k);
            if (value == null) {
                return EmptySequence.getInstance();
            }
            if (value instanceof NodeInfo) {
                return (NodeInfo)value;
            }
            List nodes = (List)value;
            return SequenceExtent.makeSequenceExtent(nodes);
        }
        return EmptySequence.getInstance();
    }

    @Override
    public int size() {
        try {
            return Count.count(this.keys());
        } catch (XPathException err) {
            return 0;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.keys().next() == null;
    }

    public AtomicIterator keys() {
        return new RangeKeyIterator();
    }

    @Override
    public Iterable<KeyValuePair> keyValuePairs() {
        return new Iterable<KeyValuePair>(){

            @Override
            public Iterator<KeyValuePair> iterator() {
                return new Iterator<KeyValuePair>(){
                    AtomicIterator keys;
                    AtomicValue next;
                    {
                        this.keys = RangeKey.this.keys();
                        this.next = this.keys.next();
                    }

                    @Override
                    public boolean hasNext() {
                        return this.next != null;
                    }

                    @Override
                    public KeyValuePair next() {
                        if (this.next == null) {
                            return null;
                        }
                        KeyValuePair kvp = new KeyValuePair(this.next, RangeKey.this.get(this.next));
                        this.next = this.keys.next();
                        return kvp;
                    }
                };
            }
        };
    }

    @Override
    public MapItem remove(AtomicValue key) {
        return HashTrieMap.copy(this).remove(key);
    }

    @Override
    public UType getKeyUType() {
        return UType.STRING;
    }

    @Override
    public MapItem addEntry(AtomicValue key, GroundedValue value) {
        return HashTrieMap.copy(this).addEntry(key, value);
    }

    @Override
    public boolean conforms(AtomicType keyType, SequenceType valueType, TypeHierarchy th) {
        Item key;
        AtomicIterator keyIter = this.keys();
        while ((key = keyIter.next()) != null) {
            GroundedValue value = this.get((AtomicValue)key);
            try {
                if (valueType.matches(value, th)) continue;
                return false;
            } catch (XPathException e) {
                throw new AssertionError((Object)e);
            }
        }
        return true;
    }

    @Override
    public MapType getItemType(TypeHierarchy th) {
        return new MapType(BuiltInAtomicType.STRING, SequenceType.NODE_SEQUENCE);
    }

    @Override
    public MapType getFunctionItemType() {
        return new MapType(BuiltInAtomicType.STRING, SequenceType.NODE_SEQUENCE);
    }

    @Override
    public String getDescription() {
        return "range key";
    }

    @Override
    public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) {
        if (other instanceof RangeKey) {
            RangeKey rk = (RangeKey)other;
            return this.min.equals(rk.min) && this.max.equals(rk.max) && this.index.equals(rk.index);
        }
        return false;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("range-key-map");
        out.emitAttribute("size", this.size() + "");
        out.endElement();
    }

    @Override
    public boolean isTrustedResultType() {
        return false;
    }

    public String toString() {
        return MapItem.mapToString(this);
    }

    private class RangeKeyIterator
    implements AtomicIterator {
        int pos = 0;
        UnicodeString curr = null;
        UnicodeString top;

        public RangeKeyIterator() {
            this.top = (UnicodeString)(RangeKey.this.max == null ? (AtomicMatchKey)RangeKey.this.index.lastKey() : (AtomicMatchKey)RangeKey.this.index.floorKey(RangeKey.this.max));
        }

        @Override
        public StringValue next() {
            if (this.pos <= 0) {
                if (this.pos < 0) {
                    return null;
                }
                if (RangeKey.this.min == null) {
                    this.curr = (UnicodeString)RangeKey.this.index.firstKey();
                } else {
                    this.curr = RangeKey.this.index.ceilingKey(RangeKey.this.min);
                    if (this.curr != null && RangeKey.this.max != null && this.curr.compareTo(RangeKey.this.max) > 0) {
                        this.curr = null;
                    }
                }
            } else {
                this.curr = this.curr.equals(this.top) ? null : RangeKey.this.index.higherKey(this.curr);
            }
            if (this.curr == null) {
                this.pos = -1;
                return null;
            }
            ++this.pos;
            return new StringValue(this.curr);
        }
    }
}

