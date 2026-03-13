/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.sort.AtomicMatchKey;
import net.sf.saxon.expr.sort.LocalOrderComparer;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.KeyDefinitionSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.ManualIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.UntypedAtomicValue;

public class KeyIndex {
    private Map<AtomicMatchKey, Object> index;
    private UType keyTypesPresent = UType.VOID;
    private UType keyTypesConvertedFromUntyped = UType.STRING_LIKE;
    private List<UntypedAtomicValue> untypedKeys;
    private ConversionRules rules;
    private int implicitTimezone;
    private StringCollator collation;
    private long creatingThread;
    private Status status;

    public KeyIndex(boolean isRangeKey) {
        this.index = isRangeKey ? new TreeMap() : new HashMap(100);
        this.creatingThread = Thread.currentThread().getId();
        this.status = Status.UNDER_CONSTRUCTION;
    }

    public Map<AtomicMatchKey, Object> getUnderlyingMap() {
        return this.index;
    }

    public boolean isCreatedInThisThread() {
        return this.creatingThread == Thread.currentThread().getId();
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void buildIndex(KeyDefinitionSet keySet, TreeInfo doc, XPathContext context) throws XPathException {
        List<KeyDefinition> definitions = keySet.getKeyDefinitions();
        for (int k = 0; k < definitions.size(); ++k) {
            this.constructIndex(doc, definitions.get(k), context, k == 0);
        }
        this.rules = context.getConfiguration().getConversionRules();
        this.implicitTimezone = context.getImplicitTimezone();
        this.collation = definitions.get(0).getCollation();
    }

    private void constructIndex(TreeInfo doc, KeyDefinition keydef, XPathContext context, boolean isFirst) throws XPathException {
        Pattern match = keydef.getMatch();
        XPathContextMajor xc = context.newContext();
        xc.setOrigin(keydef);
        xc.setCurrentComponent(keydef.getDeclaringComponent());
        xc.setTemporaryOutputState(165);
        SlotManager map = keydef.getStackFrameMap();
        if (map != null) {
            xc.openStackFrame(map);
        }
        match.selectNodes(doc, xc).forEachOrFail(node -> this.processNode((NodeInfo)node, keydef, xc, isFirst));
    }

    private void processNode(NodeInfo node, KeyDefinition keydef, XPathContext xc, boolean isFirst) throws XPathException {
        ManualIterator si = new ManualIterator(node);
        xc.setCurrentIterator(si);
        StringCollator collation = keydef.getCollation();
        int implicitTimezone = xc.getImplicitTimezone();
        Expression use = keydef.getUse();
        SequenceIterator useval = use.iterate(xc);
        if (keydef.isComposite()) {
            ArrayList<AtomicMatchKey> amks = new ArrayList<AtomicMatchKey>(4);
            useval.forEachOrFail(keyVal -> amks.add(KeyIndex.getCollationKey((AtomicValue)keyVal, collation, implicitTimezone)));
            this.addEntry(new CompositeAtomicMatchKey(amks), node, isFirst);
        } else {
            AtomicValue keyVal2;
            while ((keyVal2 = (AtomicValue)useval.next()) != null) {
                if (keyVal2.isNaN()) continue;
                UType actualUType = keyVal2.getUType();
                if (!this.keyTypesPresent.subsumes(actualUType)) {
                    this.keyTypesPresent = this.keyTypesPresent.union(actualUType);
                }
                AtomicMatchKey amk = KeyIndex.getCollationKey(keyVal2, collation, implicitTimezone);
                if (actualUType.equals(UType.UNTYPED_ATOMIC) && keydef.isConvertUntypedToOther()) {
                    if (this.untypedKeys == null) {
                        this.untypedKeys = new ArrayList<UntypedAtomicValue>(20);
                    }
                    this.untypedKeys.add((UntypedAtomicValue)keyVal2);
                }
                this.addEntry(amk, node, isFirst);
            }
        }
    }

    private void addEntry(AtomicMatchKey val, NodeInfo curr, boolean isFirst) {
        Object value = this.index.get(val);
        if (value == null) {
            this.index.put(val, curr);
        } else {
            ArrayList<NodeInfo> nodes;
            if (value instanceof NodeInfo) {
                nodes = new ArrayList<NodeInfo>(4);
                nodes.add((NodeInfo)value);
                this.index.put(val, nodes);
            } else {
                nodes = (ArrayList<NodeInfo>)value;
            }
            if (isFirst) {
                if (nodes.get(nodes.size() - 1) != curr) {
                    nodes.add(curr);
                }
            } else {
                LocalOrderComparer comparer = LocalOrderComparer.getInstance();
                boolean found = false;
                for (int i = nodes.size() - 1; i >= 0; --i) {
                    int d = comparer.compare(curr, (Item)nodes.get(i));
                    if (d < 0) continue;
                    if (d != 0) {
                        nodes.add(i + 1, curr);
                    }
                    found = true;
                    break;
                }
                if (!found) {
                    nodes.add(0, curr);
                }
            }
        }
    }

    public void reindexUntypedValues(BuiltInAtomicType type) throws XPathException {
        UType uType = type.getUType();
        if (UType.STRING_LIKE.subsumes(uType)) {
            return;
        }
        if (UType.NUMERIC.subsumes(uType)) {
            type = BuiltInAtomicType.DOUBLE;
        }
        StringConverter converter = type.getStringConverter(this.rules);
        for (UntypedAtomicValue v : this.untypedKeys) {
            AtomicMatchKey uk = KeyIndex.getCollationKey(v, this.collation, this.implicitTimezone);
            AtomicValue convertedValue = converter.convertString(v.getStringValueCS()).asAtomic();
            AtomicMatchKey amk = KeyIndex.getCollationKey(convertedValue, this.collation, this.implicitTimezone);
            Object value = this.index.get(uk);
            if (value instanceof NodeInfo) {
                this.addEntry(amk, (NodeInfo)value, false);
                continue;
            }
            List nodes = (List)value;
            for (NodeInfo node : nodes) {
                this.addEntry(amk, node, false);
            }
        }
    }

    public boolean isEmpty() {
        return this.index.isEmpty();
    }

    public SequenceIterator getNodes(AtomicValue soughtValue) throws XPathException {
        if (this.untypedKeys != null && !this.keyTypesConvertedFromUntyped.subsumes(soughtValue.getUType())) {
            this.reindexUntypedValues(soughtValue.getPrimitiveType());
        }
        Object value = this.index.get(KeyIndex.getCollationKey(soughtValue, this.collation, this.implicitTimezone));
        return this.entryIterator(value);
    }

    private SequenceIterator entryIterator(Object value) {
        if (value == null) {
            return EmptyIterator.ofNodes();
        }
        if (value instanceof NodeInfo) {
            return SingleNodeIterator.makeIterator((NodeInfo)value);
        }
        List nodes = (List)value;
        return new ListIterator(nodes);
    }

    public SequenceIterator getComposite(SequenceIterator soughtValue) throws XPathException {
        ArrayList<AtomicMatchKey> amks = new ArrayList<AtomicMatchKey>(4);
        soughtValue.forEachOrFail(keyVal -> amks.add(KeyIndex.getCollationKey((AtomicValue)keyVal, this.collation, this.implicitTimezone)));
        Object value = this.index.get(new CompositeAtomicMatchKey(amks));
        return this.entryIterator(value);
    }

    private static AtomicMatchKey getCollationKey(AtomicValue value, StringCollator collation, int implicitTimezone) throws XPathException {
        if (UType.STRING_LIKE.subsumes(value.getUType())) {
            if (collation == null) {
                return UnicodeString.makeUnicodeString(value.getStringValueCS());
            }
            return collation.getCollationKey(value.getStringValue());
        }
        return value.getXPathComparable(false, collation, implicitTimezone);
    }

    private class CompositeAtomicMatchKey
    implements AtomicMatchKey {
        private List<AtomicMatchKey> keys;

        public CompositeAtomicMatchKey(List<AtomicMatchKey> keys) {
            this.keys = keys;
        }

        @Override
        public AtomicValue asAtomic() {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object obj) {
            if (obj instanceof CompositeAtomicMatchKey && ((CompositeAtomicMatchKey)obj).keys.size() == this.keys.size()) {
                List<AtomicMatchKey> keys2 = ((CompositeAtomicMatchKey)obj).keys;
                for (int i = 0; i < this.keys.size(); ++i) {
                    if (this.keys.get(i).equals(keys2.get(i))) continue;
                    return false;
                }
                return true;
            }
            return false;
        }

        public int hashCode() {
            int h = -1968014122;
            for (AtomicMatchKey amk : this.keys) {
                h ^= amk.hashCode();
                h <<= 1;
            }
            return h;
        }
    }

    public static enum Status {
        UNDER_CONSTRUCTION,
        BUILT,
        FAILED;

    }
}

