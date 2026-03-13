/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

public interface MapItem
extends Function {
    public GroundedValue get(AtomicValue var1);

    public int size();

    public boolean isEmpty();

    public AtomicIterator<? extends AtomicValue> keys();

    public Iterable<KeyValuePair> keyValuePairs();

    public MapItem addEntry(AtomicValue var1, GroundedValue var2);

    public MapItem remove(AtomicValue var1);

    public boolean conforms(AtomicType var1, SequenceType var2, TypeHierarchy var3);

    public ItemType getItemType(TypeHierarchy var1);

    public UType getKeyUType();

    @Override
    default public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append("map{");
        int size = this.size();
        if (size == 0) {
            sb.append("}");
        } else if (size <= 5) {
            int pos = 0;
            for (KeyValuePair pair : this.keyValuePairs()) {
                if (pos++ > 0) {
                    sb.append(",");
                }
                sb.append(Err.depict(pair.key)).append(":").append(Err.depictSequence(pair.value));
            }
            sb.append("}");
        } else {
            sb.append("(:size ").append(size).append(":)}");
        }
        return sb.toString();
    }

    @Override
    default public Genre getGenre() {
        return Genre.MAP;
    }

    @Override
    default public boolean isArray() {
        return false;
    }

    @Override
    default public boolean isMap() {
        return true;
    }

    @Override
    default public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
    }

    @Override
    default public AtomicSequence atomize() throws XPathException {
        throw new XPathException("Cannot atomize a map (" + this.toShortString() + ")", "FOTY0013");
    }

    public static boolean isKnownToConform(Sequence value, ItemType itemType) {
        if (itemType == AnyItemType.getInstance()) {
            return true;
        }
        try {
            Item item;
            SequenceIterator iter = value.iterate();
            while ((item = iter.next()) != null) {
                if (item instanceof AtomicValue) {
                    if (itemType instanceof AtomicType) {
                        if (Type.isSubType(((AtomicValue)item).getItemType(), (AtomicType)itemType)) continue;
                        return false;
                    }
                    return false;
                }
                if (item instanceof NodeInfo) {
                    if (itemType instanceof NodeTest) {
                        if (((NodeTest)itemType).test((NodeInfo)item)) continue;
                        return false;
                    }
                    return false;
                }
                return false;
            }
            return true;
        } catch (XPathException e) {
            return false;
        }
    }

    public static ItemType getItemTypeOfSequence(Sequence val) {
        try {
            Item first = val.head();
            if (first == null) {
                return AnyItemType.getInstance();
            }
            ItemType type = first instanceof AtomicValue ? ((AtomicValue)first).getItemType() : (first instanceof NodeInfo ? NodeKindTest.makeNodeKindTest(((NodeInfo)first).getNodeKind()) : AnyFunctionType.getInstance());
            if (MapItem.isKnownToConform(val, type)) {
                return type;
            }
            return AnyItemType.getInstance();
        } catch (XPathException e) {
            return AnyItemType.getInstance();
        }
    }

    @Override
    default public OperandRole[] getOperandRoles() {
        return new OperandRole[]{OperandRole.SINGLE_ATOMIC};
    }

    @Override
    default public FunctionItemType getFunctionItemType() {
        return MapType.ANY_MAP_TYPE;
    }

    @Override
    default public StructuredQName getFunctionName() {
        return null;
    }

    @Override
    default public String getDescription() {
        return "map";
    }

    @Override
    default public int getArity() {
        return 1;
    }

    @Override
    default public XPathContext makeNewContext(XPathContext callingContext, ContextOriginator originator) {
        return callingContext;
    }

    @Override
    default public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
        AtomicValue key = (AtomicValue)args[0].head();
        GroundedValue value = this.get(key);
        if (value == null) {
            return EmptySequence.getInstance();
        }
        return value;
    }

    @Override
    default public String getStringValue() {
        throw new UnsupportedOperationException("A map has no string value");
    }

    @Override
    default public CharSequence getStringValueCS() {
        throw new UnsupportedOperationException("A map has no string value");
    }

    default public SequenceIterator getTypedValue() throws XPathException {
        throw new XPathException("A map has no typed value");
    }

    @Override
    default public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) throws XPathException {
        if (other instanceof MapItem && ((MapItem)other).size() == this.size()) {
            Item key;
            AtomicIterator<? extends AtomicValue> keys = this.keys();
            while ((key = keys.next()) != null) {
                GroundedValue thisValue = this.get((AtomicValue)key);
                GroundedValue otherValue = ((MapItem)other).get((AtomicValue)key);
                if (otherValue == null) {
                    return false;
                }
                if (DeepEqual.deepEqual(otherValue.iterate(), thisValue.iterate(), comparer, context, flags)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    default public MapItem itemAt(int n) {
        return n == 0 ? this : null;
    }

    @Override
    default public boolean effectiveBooleanValue() throws XPathException {
        throw new XPathException("A map item has no effective boolean value");
    }

    public static String mapToString(MapItem map) {
        FastStringBuffer buffer = new FastStringBuffer(256);
        buffer.append("map{");
        for (KeyValuePair pair : map.keyValuePairs()) {
            if (buffer.length() > 4) {
                buffer.append(",");
            }
            buffer.append(pair.key.toString());
            buffer.append(":");
            buffer.append(pair.value.toString());
        }
        buffer.append("}");
        return buffer.toString();
    }

    @Override
    default public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("map");
        out.emitAttribute("size", "" + this.size());
        for (KeyValuePair kvp : this.keyValuePairs()) {
            Literal.exportAtomicValue(kvp.key, out);
            Literal.exportValue(kvp.value, out);
        }
        out.endElement();
    }

    @Override
    default public boolean isTrustedResultType() {
        return true;
    }
}

