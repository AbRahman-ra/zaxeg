/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.Configuration;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.DocumentNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.SameNameTest;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.JavaExternalObjectType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceType;

public abstract class Type {
    public static final short ELEMENT = 1;
    public static final short ATTRIBUTE = 2;
    public static final short TEXT = 3;
    public static final short WHITESPACE_TEXT = 4;
    public static final short PROCESSING_INSTRUCTION = 7;
    public static final short COMMENT = 8;
    public static final short DOCUMENT = 9;
    public static final short NAMESPACE = 13;
    public static final short STOPPER = 11;
    public static final short PARENT_POINTER = 12;
    public static final short TEXTUAL_ELEMENT = 17;
    public static final short NODE = 0;
    public static final ItemType NODE_TYPE = AnyNodeTest.getInstance();
    public static final short ITEM = 88;
    public static final ItemType ITEM_TYPE = AnyItemType.getInstance();
    public static final short FUNCTION = 99;

    private Type() {
    }

    public static boolean isNodeType(ItemType type) {
        return type instanceof NodeTest;
    }

    public static ItemType getItemType(Item item, TypeHierarchy th) {
        if (item == null) {
            return AnyItemType.getInstance();
        }
        if (item instanceof AtomicValue) {
            return ((AtomicValue)item).getItemType();
        }
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            if (th == null) {
                th = node.getConfiguration().getTypeHierarchy();
            }
            switch (node.getNodeKind()) {
                case 9: {
                    ItemType elementType = null;
                    for (NodeInfo nodeInfo : node.children()) {
                        int kind = nodeInfo.getNodeKind();
                        if (kind == 3) {
                            elementType = null;
                            break;
                        }
                        if (kind != 1) continue;
                        if (elementType != null) {
                            elementType = null;
                            break;
                        }
                        elementType = Type.getItemType(nodeInfo, th);
                    }
                    if (elementType == null) {
                        return NodeKindTest.DOCUMENT;
                    }
                    return new DocumentNodeTest((NodeTest)elementType);
                }
                case 1: {
                    SchemaType eltype = node.getSchemaType();
                    if (eltype.equals(Untyped.getInstance()) || eltype.equals(AnyType.getInstance())) {
                        return new SameNameTest(node);
                    }
                    return new CombinedNodeTest(new SameNameTest(node), 23, new ContentTypeTest(1, eltype, node.getConfiguration(), false));
                }
                case 2: {
                    SchemaType schemaType = node.getSchemaType();
                    if (schemaType.equals(BuiltInAtomicType.UNTYPED_ATOMIC)) {
                        return new SameNameTest(node);
                    }
                    return new CombinedNodeTest(new SameNameTest(node), 23, new ContentTypeTest(2, schemaType, node.getConfiguration(), false));
                }
                case 3: {
                    return NodeKindTest.TEXT;
                }
                case 8: {
                    return NodeKindTest.COMMENT;
                }
                case 7: {
                    return NodeKindTest.PROCESSING_INSTRUCTION;
                }
                case 13: {
                    return NodeKindTest.NAMESPACE;
                }
            }
            throw new IllegalArgumentException("Unknown node kind " + node.getNodeKind());
        }
        if (item instanceof ExternalObject) {
            if (th == null) {
                throw new IllegalArgumentException("typeHierarchy is required for an external object");
            }
            return ((ExternalObject)item).getItemType(th);
        }
        if (item instanceof MapItem) {
            return th == null ? MapType.ANY_MAP_TYPE : ((MapItem)item).getItemType(th);
        }
        if (item instanceof ArrayItem) {
            return th == null ? ArrayItemType.ANY_ARRAY_TYPE : new ArrayItemType(((ArrayItem)item).getMemberType(th));
        }
        return ((Function)item).getFunctionItemType();
    }

    public static String displayTypeName(Item item) {
        if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            switch (node.getNodeKind()) {
                case 9: {
                    return "document-node()";
                }
                case 1: {
                    SchemaType annotation = node.getSchemaType();
                    return "element(" + ((NodeInfo)item).getDisplayName() + ", " + annotation.getDisplayName() + ')';
                }
                case 2: {
                    SchemaType annotation2 = node.getSchemaType();
                    return "attribute(" + ((NodeInfo)item).getDisplayName() + ", " + annotation2.getDisplayName() + ')';
                }
                case 3: {
                    return "text()";
                }
                case 8: {
                    return "comment()";
                }
                case 7: {
                    return "processing-instruction()";
                }
                case 13: {
                    return "namespace()";
                }
            }
            return "";
        }
        if (item instanceof ExternalObject) {
            return ObjectValue.displayTypeName(((ExternalObject)item).getObject());
        }
        if (item instanceof AtomicValue) {
            return ((AtomicValue)item).getItemType().toString();
        }
        if (item instanceof Function) {
            return "function(*)";
        }
        return item.getClass().toString();
    }

    public static ItemType getBuiltInItemType(String namespace, String localName) {
        SchemaType t = BuiltInType.getSchemaType(StandardNames.getFingerprint(namespace, localName));
        if (t instanceof ItemType) {
            return (ItemType)((Object)t);
        }
        return null;
    }

    public static SimpleType getBuiltInSimpleType(String namespace, String localName) {
        SchemaType t = BuiltInType.getSchemaType(StandardNames.getFingerprint(namespace, localName));
        if (t instanceof SimpleType && ((SimpleType)t).isBuiltInType()) {
            return (SimpleType)t;
        }
        return null;
    }

    public static boolean isSubType(AtomicType one, AtomicType two) {
        while (true) {
            if (one.getFingerprint() == two.getFingerprint()) {
                return true;
            }
            SchemaType s = one.getBaseType();
            if (!(s instanceof AtomicType)) break;
            one = (AtomicType)s;
        }
        return false;
    }

    public static ItemType getCommonSuperType(ItemType t1, ItemType t2, TypeHierarchy th) {
        if (t1 == t2) {
            return t1;
        }
        if (t1 instanceof ErrorType) {
            return t2;
        }
        if (t2 instanceof ErrorType) {
            return t1;
        }
        if (t1 instanceof JavaExternalObjectType && t2 instanceof JavaExternalObjectType) {
            Configuration config = ((JavaExternalObjectType)t1).getConfiguration();
            Class<?> c1 = ((JavaExternalObjectType)t1).getJavaClass();
            Class<?> c2 = ((JavaExternalObjectType)t2).getJavaClass();
            return config.getJavaExternalObjectType(Type.leastCommonSuperClass(c1, c2));
        }
        if (t1 instanceof MapType && t2 instanceof MapType) {
            if (t1 == MapType.EMPTY_MAP_TYPE) {
                return t2;
            }
            if (t2 == MapType.EMPTY_MAP_TYPE) {
                return t1;
            }
            ItemType keyType = Type.getCommonSuperType(((MapType)t1).getKeyType(), ((MapType)t2).getKeyType());
            AtomicType k = keyType instanceof AtomicType ? (AtomicType)keyType : keyType.getAtomizedItemType().getPrimitiveItemType();
            SequenceType v = SequenceType.makeSequenceType(Type.getCommonSuperType(((MapType)t1).getValueType().getPrimaryType(), ((MapType)t2).getValueType().getPrimaryType()), Cardinality.union(((MapType)t1).getValueType().getCardinality(), ((MapType)t2).getValueType().getCardinality()));
            return new MapType(k, v);
        }
        Affinity r = th.relationship(t1, t2);
        if (r == Affinity.SAME_TYPE) {
            return t1;
        }
        if (r == Affinity.SUBSUMED_BY) {
            return t2;
        }
        if (r == Affinity.SUBSUMES) {
            return t1;
        }
        return t1.getUType().union(t2.getUType()).toItemType();
    }

    public static ItemType getCommonSuperType(ItemType t1, ItemType t2) {
        ItemType p2;
        if (t1 == t2) {
            return t1;
        }
        if (t1 instanceof ErrorType) {
            return t2;
        }
        if (t2 instanceof ErrorType) {
            return t1;
        }
        if (t1 == AnyItemType.getInstance() || t2 == AnyItemType.getInstance()) {
            return AnyItemType.getInstance();
        }
        ItemType p1 = t1.getPrimitiveItemType();
        if (p1 == (p2 = t2.getPrimitiveItemType())) {
            return p1;
        }
        if (p1 == BuiltInAtomicType.DECIMAL && p2 == BuiltInAtomicType.INTEGER || p2 == BuiltInAtomicType.DECIMAL && p1 == BuiltInAtomicType.INTEGER) {
            return BuiltInAtomicType.DECIMAL;
        }
        if (p1 instanceof BuiltInAtomicType && ((BuiltInAtomicType)p1).isNumericType() && p2 instanceof BuiltInAtomicType && ((BuiltInAtomicType)p2).isNumericType()) {
            return NumericType.getInstance();
        }
        if (t1.isAtomicType() && t2.isAtomicType()) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        if (t1 instanceof NodeTest && t2 instanceof NodeTest) {
            return AnyNodeTest.getInstance();
        }
        if (t1 instanceof JavaExternalObjectType && t2 instanceof JavaExternalObjectType) {
            Configuration config = ((JavaExternalObjectType)t1).getConfiguration();
            Class<?> c1 = ((JavaExternalObjectType)t1).getJavaClass();
            Class<?> c2 = ((JavaExternalObjectType)t2).getJavaClass();
            return config.getJavaExternalObjectType(Type.leastCommonSuperClass(c1, c2));
        }
        return AnyItemType.getInstance();
    }

    public static SequenceType getCommonSuperType(SequenceType t1, SequenceType t2) {
        if (t1.equals(t2)) {
            return t1;
        }
        return SequenceType.makeSequenceType(Type.getCommonSuperType(t1.getPrimaryType(), t2.getPrimaryType()), Cardinality.union(t1.getCardinality(), t2.getCardinality()));
    }

    private static Class<?> leastCommonSuperClass(Class<?> class1, Class<?> class2) {
        if (class1 == class2) {
            return class1;
        }
        if (class1 == null || class2 == null) {
            return null;
        }
        if (!class1.isArray() && class1.isAssignableFrom(class2)) {
            return class1;
        }
        if (!class2.isArray() && class2.isAssignableFrom(class1)) {
            return class2;
        }
        if (class1.isInterface() || class2.isInterface()) {
            return Object.class;
        }
        return Type.leastCommonSuperClass(class1.getSuperclass(), class2.getSuperclass());
    }

    public static boolean isPrimitiveAtomicType(int fingerprint) {
        return fingerprint >= 0 && (fingerprint <= 533 || fingerprint == 635 || fingerprint == 631 || fingerprint == 632 || fingerprint == 634 || fingerprint == 633 || fingerprint == 573);
    }

    public static boolean isPrimitiveAtomicUType(int fingerprint) {
        return fingerprint >= 0 && fingerprint <= 533;
    }

    public static boolean isGuaranteedComparable(BuiltInAtomicType t1, BuiltInAtomicType t2, boolean ordered) {
        if (t1 == t2) {
            return true;
        }
        if (t1.isPrimitiveNumeric()) {
            return t2.isPrimitiveNumeric();
        }
        if (t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t1.equals(BuiltInAtomicType.ANY_URI)) {
            t1 = BuiltInAtomicType.STRING;
        }
        if (t2.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t2.equals(BuiltInAtomicType.ANY_URI)) {
            t2 = BuiltInAtomicType.STRING;
        }
        if (!ordered) {
            if (t1.equals(BuiltInAtomicType.DAY_TIME_DURATION)) {
                t1 = BuiltInAtomicType.DURATION;
            }
            if (t2.equals(BuiltInAtomicType.DAY_TIME_DURATION)) {
                t2 = BuiltInAtomicType.DURATION;
            }
            if (t1.equals(BuiltInAtomicType.YEAR_MONTH_DURATION)) {
                t1 = BuiltInAtomicType.DURATION;
            }
            if (t2.equals(BuiltInAtomicType.YEAR_MONTH_DURATION)) {
                t2 = BuiltInAtomicType.DURATION;
            }
        }
        return t1 == t2;
    }

    public static boolean isPossiblyComparable(BuiltInAtomicType t1, BuiltInAtomicType t2, boolean ordered) {
        if (t1 == t2) {
            return true;
        }
        if (t1.equals(BuiltInAtomicType.ANY_ATOMIC) || t2.equals(BuiltInAtomicType.ANY_ATOMIC)) {
            return true;
        }
        if (t1.isPrimitiveNumeric()) {
            return t2.isPrimitiveNumeric();
        }
        if (t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t1.equals(BuiltInAtomicType.ANY_URI)) {
            t1 = BuiltInAtomicType.STRING;
        }
        if (t2.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t2.equals(BuiltInAtomicType.ANY_URI)) {
            t2 = BuiltInAtomicType.STRING;
        }
        if (t1.equals(BuiltInAtomicType.DAY_TIME_DURATION)) {
            t1 = BuiltInAtomicType.DURATION;
        }
        if (t2.equals(BuiltInAtomicType.DAY_TIME_DURATION)) {
            t2 = BuiltInAtomicType.DURATION;
        }
        if (t1.equals(BuiltInAtomicType.YEAR_MONTH_DURATION)) {
            t1 = BuiltInAtomicType.DURATION;
        }
        if (t2.equals(BuiltInAtomicType.YEAR_MONTH_DURATION)) {
            t2 = BuiltInAtomicType.DURATION;
        }
        return t1 == t2;
    }

    public static boolean isGenerallyComparable(BuiltInAtomicType t1, BuiltInAtomicType t2, boolean ordered) {
        return t1.equals(BuiltInAtomicType.ANY_ATOMIC) || t2.equals(BuiltInAtomicType.ANY_ATOMIC) || t1.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || t2.equals(BuiltInAtomicType.UNTYPED_ATOMIC) || Type.isGuaranteedComparable(t1, t2, ordered);
    }

    public static boolean isGuaranteedGenerallyComparable(BuiltInAtomicType t1, BuiltInAtomicType t2, boolean ordered) {
        return !t1.equals(BuiltInAtomicType.ANY_ATOMIC) && !t2.equals(BuiltInAtomicType.ANY_ATOMIC) && Type.isGenerallyComparable(t1, t2, ordered);
    }
}

