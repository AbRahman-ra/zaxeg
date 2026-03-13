/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.ArrayItemType;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.NumericValue;

public abstract class ItemType {
    private static ConversionRules defaultConversionRules = new ConversionRules();
    public static ItemType ANY_ITEM;
    public static ItemType ANY_FUNCTION;
    public static final ItemType ANY_NODE;
    public static final ItemType ATTRIBUTE_NODE;
    public static final ItemType COMMENT_NODE;
    public static final ItemType TEXT_NODE;
    public static final ItemType ELEMENT_NODE;
    public static final ItemType DOCUMENT_NODE;
    public static final ItemType NAMESPACE_NODE;
    public static final ItemType PROCESSING_INSTRUCTION_NODE;
    public static final ItemType ANY_MAP;
    public static final ItemType ANY_ARRAY;
    public static final ItemType ANY_ATOMIC_VALUE;
    public static final ItemType ERROR;
    public static final ItemType STRING;
    public static final ItemType BOOLEAN;
    public static final ItemType DURATION;
    public static final ItemType DATE_TIME;
    public static final ItemType DATE;
    public static final ItemType TIME;
    public static final ItemType G_YEAR_MONTH;
    public static final ItemType G_MONTH;
    public static final ItemType G_MONTH_DAY;
    public static final ItemType G_YEAR;
    public static final ItemType G_DAY;
    public static final ItemType HEX_BINARY;
    public static final ItemType BASE64_BINARY;
    public static final ItemType ANY_URI;
    public static final ItemType QNAME;
    public static final ItemType NOTATION;
    public static final ItemType UNTYPED_ATOMIC;
    public static final ItemType DECIMAL;
    public static final ItemType FLOAT;
    public static final ItemType DOUBLE;
    public static final ItemType INTEGER;
    public static final ItemType NON_POSITIVE_INTEGER;
    public static final ItemType NEGATIVE_INTEGER;
    public static final ItemType LONG;
    public static final ItemType INT;
    public static final ItemType SHORT;
    public static final ItemType BYTE;
    public static final ItemType NON_NEGATIVE_INTEGER;
    public static final ItemType POSITIVE_INTEGER;
    public static final ItemType UNSIGNED_LONG;
    public static final ItemType UNSIGNED_INT;
    public static final ItemType UNSIGNED_SHORT;
    public static final ItemType UNSIGNED_BYTE;
    public static final ItemType YEAR_MONTH_DURATION;
    public static final ItemType DAY_TIME_DURATION;
    public static final ItemType NORMALIZED_STRING;
    public static final ItemType TOKEN;
    public static final ItemType LANGUAGE;
    public static final ItemType NAME;
    public static final ItemType NMTOKEN;
    public static final ItemType NCNAME;
    public static final ItemType ID;
    public static final ItemType IDREF;
    public static final ItemType ENTITY;
    public static final ItemType DATE_TIME_STAMP;
    public static final ItemType NUMERIC;

    private static ItemType atomic(BuiltInAtomicType underlyingType, ConversionRules conversionRules) {
        return new BuiltInAtomicItemType(underlyingType, conversionRules);
    }

    public ConversionRules getConversionRules() {
        return defaultConversionRules;
    }

    public abstract boolean matches(XdmItem var1);

    public abstract boolean subsumes(ItemType var1);

    public abstract net.sf.saxon.type.ItemType getUnderlyingItemType();

    public QName getTypeName() {
        net.sf.saxon.type.ItemType type = this.getUnderlyingItemType();
        if (type instanceof SchemaType) {
            StructuredQName name = ((SchemaType)((Object)type)).getStructuredQName();
            return name == null ? null : new QName(name);
        }
        return null;
    }

    public final boolean equals(Object other) {
        return other instanceof ItemType && this.getUnderlyingItemType().equals(((ItemType)other).getUnderlyingItemType());
    }

    public final int hashCode() {
        return this.getUnderlyingItemType().hashCode();
    }

    public String toString() {
        net.sf.saxon.type.ItemType type = this.getUnderlyingItemType();
        if (type instanceof SchemaType) {
            String marker = "";
            SchemaType st = (SchemaType)((Object)type);
            do {
                StructuredQName name;
                if ((name = st.getStructuredQName()) != null) {
                    return marker + name.getEQName();
                }
                marker = "<";
            } while ((st = st.getBaseType()) != null);
            return "Q{http://www.w3.org/2001/XMLSchema}anyType";
        }
        return type.toString();
    }

    static {
        defaultConversionRules.setStringToDoubleConverter(StringToDouble.getInstance());
        defaultConversionRules.setNotationSet(null);
        defaultConversionRules.setURIChecker(StandardURIChecker.getInstance());
        ANY_ITEM = new ItemType(){

            @Override
            public ConversionRules getConversionRules() {
                return defaultConversionRules;
            }

            @Override
            public boolean matches(XdmItem item) {
                return true;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return true;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return AnyItemType.getInstance();
            }
        };
        ANY_FUNCTION = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                return item.getUnderlyingValue() instanceof Function;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType() instanceof FunctionItemType;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return AnyFunctionType.getInstance();
            }
        };
        ANY_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                return item.getUnderlyingValue() instanceof NodeInfo;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType() instanceof NodeTest;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return AnyNodeTest.getInstance();
            }
        };
        ATTRIBUTE_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 2;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.ATTRIBUTE;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.ATTRIBUTE;
            }
        };
        COMMENT_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 8;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.COMMENT;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.COMMENT;
            }
        };
        TEXT_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 3;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.TEXT;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.TEXT;
            }
        };
        ELEMENT_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 1;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.ELEMENT;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.ELEMENT;
            }
        };
        DOCUMENT_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 9;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.DOCUMENT;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.DOCUMENT;
            }
        };
        NAMESPACE_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 13;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.NAMESPACE;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.NAMESPACE;
            }
        };
        PROCESSING_INSTRUCTION_NODE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                Item it = item.getUnderlyingValue();
                return it instanceof NodeInfo && ((NodeInfo)it).getNodeKind() == 7;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType().getUType() == UType.PI;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NodeKindTest.PROCESSING_INSTRUCTION;
            }
        };
        ANY_MAP = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                return item.getUnderlyingValue() instanceof MapItem;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType() instanceof MapType;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return MapType.ANY_MAP_TYPE;
            }
        };
        ANY_ARRAY = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                return item.getUnderlyingValue() instanceof ArrayItem;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType() instanceof ArrayItemType;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return ArrayItemType.ANY_ARRAY_TYPE;
            }
        };
        ANY_ATOMIC_VALUE = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                return item.getUnderlyingValue() instanceof AtomicValue;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType() instanceof AtomicType;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return BuiltInAtomicType.ANY_ATOMIC;
            }
        };
        ERROR = new ItemType(){

            @Override
            public boolean matches(XdmItem item) {
                return false;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return other.getUnderlyingItemType() instanceof ErrorType;
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return ErrorType.getInstance();
            }
        };
        STRING = ItemType.atomic(BuiltInAtomicType.STRING, defaultConversionRules);
        BOOLEAN = ItemType.atomic(BuiltInAtomicType.BOOLEAN, defaultConversionRules);
        DURATION = ItemType.atomic(BuiltInAtomicType.DURATION, defaultConversionRules);
        DATE_TIME = ItemType.atomic(BuiltInAtomicType.DATE_TIME, defaultConversionRules);
        DATE = ItemType.atomic(BuiltInAtomicType.DATE, defaultConversionRules);
        TIME = ItemType.atomic(BuiltInAtomicType.TIME, defaultConversionRules);
        G_YEAR_MONTH = ItemType.atomic(BuiltInAtomicType.G_YEAR_MONTH, defaultConversionRules);
        G_MONTH = ItemType.atomic(BuiltInAtomicType.G_MONTH, defaultConversionRules);
        G_MONTH_DAY = ItemType.atomic(BuiltInAtomicType.G_MONTH_DAY, defaultConversionRules);
        G_YEAR = ItemType.atomic(BuiltInAtomicType.G_YEAR, defaultConversionRules);
        G_DAY = ItemType.atomic(BuiltInAtomicType.G_DAY, defaultConversionRules);
        HEX_BINARY = ItemType.atomic(BuiltInAtomicType.HEX_BINARY, defaultConversionRules);
        BASE64_BINARY = ItemType.atomic(BuiltInAtomicType.BASE64_BINARY, defaultConversionRules);
        ANY_URI = ItemType.atomic(BuiltInAtomicType.ANY_URI, defaultConversionRules);
        QNAME = ItemType.atomic(BuiltInAtomicType.QNAME, defaultConversionRules);
        NOTATION = ItemType.atomic(BuiltInAtomicType.NOTATION, defaultConversionRules);
        UNTYPED_ATOMIC = ItemType.atomic(BuiltInAtomicType.UNTYPED_ATOMIC, defaultConversionRules);
        DECIMAL = ItemType.atomic(BuiltInAtomicType.DECIMAL, defaultConversionRules);
        FLOAT = ItemType.atomic(BuiltInAtomicType.FLOAT, defaultConversionRules);
        DOUBLE = ItemType.atomic(BuiltInAtomicType.DOUBLE, defaultConversionRules);
        INTEGER = ItemType.atomic(BuiltInAtomicType.INTEGER, defaultConversionRules);
        NON_POSITIVE_INTEGER = ItemType.atomic(BuiltInAtomicType.NON_POSITIVE_INTEGER, defaultConversionRules);
        NEGATIVE_INTEGER = ItemType.atomic(BuiltInAtomicType.NEGATIVE_INTEGER, defaultConversionRules);
        LONG = ItemType.atomic(BuiltInAtomicType.LONG, defaultConversionRules);
        INT = ItemType.atomic(BuiltInAtomicType.INT, defaultConversionRules);
        SHORT = ItemType.atomic(BuiltInAtomicType.SHORT, defaultConversionRules);
        BYTE = ItemType.atomic(BuiltInAtomicType.BYTE, defaultConversionRules);
        NON_NEGATIVE_INTEGER = ItemType.atomic(BuiltInAtomicType.NON_NEGATIVE_INTEGER, defaultConversionRules);
        POSITIVE_INTEGER = ItemType.atomic(BuiltInAtomicType.POSITIVE_INTEGER, defaultConversionRules);
        UNSIGNED_LONG = ItemType.atomic(BuiltInAtomicType.UNSIGNED_LONG, defaultConversionRules);
        UNSIGNED_INT = ItemType.atomic(BuiltInAtomicType.UNSIGNED_INT, defaultConversionRules);
        UNSIGNED_SHORT = ItemType.atomic(BuiltInAtomicType.UNSIGNED_SHORT, defaultConversionRules);
        UNSIGNED_BYTE = ItemType.atomic(BuiltInAtomicType.UNSIGNED_BYTE, defaultConversionRules);
        YEAR_MONTH_DURATION = ItemType.atomic(BuiltInAtomicType.YEAR_MONTH_DURATION, defaultConversionRules);
        DAY_TIME_DURATION = ItemType.atomic(BuiltInAtomicType.DAY_TIME_DURATION, defaultConversionRules);
        NORMALIZED_STRING = ItemType.atomic(BuiltInAtomicType.NORMALIZED_STRING, defaultConversionRules);
        TOKEN = ItemType.atomic(BuiltInAtomicType.TOKEN, defaultConversionRules);
        LANGUAGE = ItemType.atomic(BuiltInAtomicType.LANGUAGE, defaultConversionRules);
        NAME = ItemType.atomic(BuiltInAtomicType.NAME, defaultConversionRules);
        NMTOKEN = ItemType.atomic(BuiltInAtomicType.NMTOKEN, defaultConversionRules);
        NCNAME = ItemType.atomic(BuiltInAtomicType.NCNAME, defaultConversionRules);
        ID = ItemType.atomic(BuiltInAtomicType.ID, defaultConversionRules);
        IDREF = ItemType.atomic(BuiltInAtomicType.IDREF, defaultConversionRules);
        ENTITY = ItemType.atomic(BuiltInAtomicType.ENTITY, defaultConversionRules);
        DATE_TIME_STAMP = ItemType.atomic(BuiltInAtomicType.DATE_TIME_STAMP, defaultConversionRules);
        NUMERIC = new ItemType(){

            @Override
            public ConversionRules getConversionRules() {
                return defaultConversionRules;
            }

            @Override
            public boolean matches(XdmItem item) {
                return item.getUnderlyingValue() instanceof NumericValue;
            }

            @Override
            public boolean subsumes(ItemType other) {
                return DECIMAL.subsumes(other) || DOUBLE.subsumes(other) || FLOAT.subsumes(other);
            }

            @Override
            public net.sf.saxon.type.ItemType getUnderlyingItemType() {
                return NumericType.getInstance();
            }
        };
    }

    static class BuiltInAtomicItemType
    extends ItemType {
        private BuiltInAtomicType underlyingType;
        private ConversionRules conversionRules;

        public BuiltInAtomicItemType(BuiltInAtomicType underlyingType, ConversionRules conversionRules) {
            this.underlyingType = underlyingType;
            this.conversionRules = conversionRules;
        }

        public static BuiltInAtomicItemType makeVariant(BuiltInAtomicItemType type, ConversionRules conversionRules) {
            return new BuiltInAtomicItemType(type.underlyingType, conversionRules);
        }

        @Override
        public ConversionRules getConversionRules() {
            return this.conversionRules;
        }

        @Override
        public boolean matches(XdmItem item) {
            Item value = item.getUnderlyingValue();
            if (!(value instanceof AtomicValue)) {
                return false;
            }
            AtomicType type = ((AtomicValue)value).getItemType();
            return this.subsumesUnderlyingType(type);
        }

        @Override
        public boolean subsumes(ItemType other) {
            net.sf.saxon.type.ItemType otherType = other.getUnderlyingItemType();
            if (!otherType.isPlainType()) {
                return false;
            }
            AtomicType type = (AtomicType)otherType;
            return this.subsumesUnderlyingType(type);
        }

        private boolean subsumesUnderlyingType(AtomicType type) {
            BuiltInAtomicType builtIn;
            BuiltInAtomicType builtInAtomicType = builtIn = type instanceof BuiltInAtomicType ? (BuiltInAtomicType)type : (BuiltInAtomicType)type.getBuiltInBaseType();
            while (!builtIn.isSameType(this.underlyingType)) {
                SchemaType base = builtIn.getBaseType();
                if (!(base instanceof BuiltInAtomicType)) {
                    return false;
                }
                builtIn = (BuiltInAtomicType)base;
            }
            return true;
        }

        @Override
        public net.sf.saxon.type.ItemType getUnderlyingItemType() {
            return this.underlyingType;
        }
    }
}

