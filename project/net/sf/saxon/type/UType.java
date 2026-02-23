/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.tree.iter.UnfailingIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.NumericType;
import net.sf.saxon.type.PrimitiveUType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;

public class UType {
    public static final UType VOID = new UType(0);
    public static final UType DOCUMENT = PrimitiveUType.DOCUMENT.toUType();
    public static final UType ELEMENT = PrimitiveUType.ELEMENT.toUType();
    public static final UType ATTRIBUTE = PrimitiveUType.ATTRIBUTE.toUType();
    public static final UType TEXT = PrimitiveUType.TEXT.toUType();
    public static final UType COMMENT = PrimitiveUType.COMMENT.toUType();
    public static final UType PI = PrimitiveUType.PI.toUType();
    public static final UType NAMESPACE = PrimitiveUType.NAMESPACE.toUType();
    public static final UType FUNCTION = PrimitiveUType.FUNCTION.toUType();
    public static final UType STRING = PrimitiveUType.STRING.toUType();
    public static final UType BOOLEAN = PrimitiveUType.BOOLEAN.toUType();
    public static final UType DECIMAL = PrimitiveUType.DECIMAL.toUType();
    public static final UType FLOAT = PrimitiveUType.FLOAT.toUType();
    public static final UType DOUBLE = PrimitiveUType.DOUBLE.toUType();
    public static final UType DURATION = PrimitiveUType.DURATION.toUType();
    public static final UType DATE_TIME = PrimitiveUType.DATE_TIME.toUType();
    public static final UType TIME = PrimitiveUType.TIME.toUType();
    public static final UType DATE = PrimitiveUType.DATE.toUType();
    public static final UType G_YEAR_MONTH = PrimitiveUType.G_YEAR_MONTH.toUType();
    public static final UType G_YEAR = PrimitiveUType.G_YEAR.toUType();
    public static final UType G_MONTH_DAY = PrimitiveUType.G_MONTH_DAY.toUType();
    public static final UType G_DAY = PrimitiveUType.G_DAY.toUType();
    public static final UType G_MONTH = PrimitiveUType.G_MONTH.toUType();
    public static final UType HEX_BINARY = PrimitiveUType.HEX_BINARY.toUType();
    public static final UType BASE64_BINARY = PrimitiveUType.BASE64_BINARY.toUType();
    public static final UType ANY_URI = PrimitiveUType.ANY_URI.toUType();
    public static final UType QNAME = PrimitiveUType.QNAME.toUType();
    public static final UType NOTATION = PrimitiveUType.NOTATION.toUType();
    public static final UType UNTYPED_ATOMIC = PrimitiveUType.UNTYPED_ATOMIC.toUType();
    public static final UType EXTENSION = PrimitiveUType.EXTENSION.toUType();
    public static final UType NUMERIC = DOUBLE.union(FLOAT).union(DECIMAL);
    public static final UType STRING_LIKE = STRING.union(ANY_URI).union(UNTYPED_ATOMIC);
    public static final UType CHILD_NODE_KINDS = ELEMENT.union(TEXT).union(COMMENT).union(PI);
    public static final UType PARENT_NODE_KINDS = DOCUMENT.union(ELEMENT);
    public static final UType ELEMENT_OR_ATTRIBUTE = ELEMENT.union(ATTRIBUTE);
    public static final UType ANY_NODE = CHILD_NODE_KINDS.union(DOCUMENT).union(ATTRIBUTE).union(NAMESPACE);
    public static final UType ANY_ATOMIC = new UType(0xFFFFF00);
    public static final UType ANY = ANY_NODE.union(ANY_ATOMIC).union(FUNCTION).union(EXTENSION);
    private int bits;

    public UType(int bits) {
        this.bits = bits;
    }

    public int hashCode() {
        return this.bits;
    }

    public boolean equals(Object obj) {
        return obj instanceof UType && this.bits == ((UType)obj).bits;
    }

    public UType union(UType other) {
        if (other == null) {
            new NullPointerException().printStackTrace();
        }
        return new UType(this.bits | other.bits);
    }

    public UType intersection(UType other) {
        return new UType(this.bits & other.bits);
    }

    public UType except(UType other) {
        return new UType(this.bits & ~other.bits);
    }

    public static UType fromTypeCode(int code) {
        switch (code) {
            case 0: {
                return ANY_NODE;
            }
            case 1: {
                return ELEMENT;
            }
            case 2: {
                return ATTRIBUTE;
            }
            case 3: 
            case 4: {
                return TEXT;
            }
            case 9: {
                return DOCUMENT;
            }
            case 8: {
                return COMMENT;
            }
            case 7: {
                return PI;
            }
            case 13: {
                return NAMESPACE;
            }
            case 99: {
                return FUNCTION;
            }
            case 88: {
                return ANY;
            }
            case 632: {
                return ANY_ATOMIC;
            }
            case 635: {
                return NUMERIC;
            }
            case 513: {
                return STRING;
            }
            case 514: {
                return BOOLEAN;
            }
            case 518: {
                return DURATION;
            }
            case 519: {
                return DATE_TIME;
            }
            case 521: {
                return DATE;
            }
            case 520: {
                return TIME;
            }
            case 522: {
                return G_YEAR_MONTH;
            }
            case 526: {
                return G_MONTH;
            }
            case 524: {
                return G_MONTH_DAY;
            }
            case 523: {
                return G_YEAR;
            }
            case 525: {
                return G_DAY;
            }
            case 527: {
                return HEX_BINARY;
            }
            case 528: {
                return BASE64_BINARY;
            }
            case 529: {
                return ANY_URI;
            }
            case 530: {
                return QNAME;
            }
            case 531: {
                return NOTATION;
            }
            case 631: {
                return UNTYPED_ATOMIC;
            }
            case 515: {
                return DECIMAL;
            }
            case 516: {
                return FLOAT;
            }
            case 517: {
                return DOUBLE;
            }
            case 533: {
                return DECIMAL;
            }
            case 534: 
            case 535: 
            case 536: 
            case 537: 
            case 538: 
            case 539: 
            case 540: 
            case 541: 
            case 542: 
            case 543: 
            case 544: 
            case 545: {
                return DECIMAL;
            }
            case 633: 
            case 634: {
                return DURATION;
            }
            case 565: {
                return DATE_TIME;
            }
            case 553: 
            case 554: 
            case 555: 
            case 556: 
            case 558: 
            case 559: 
            case 560: 
            case 561: 
            case 563: {
                return STRING;
            }
        }
        throw new IllegalArgumentException("" + code);
    }

    public Set<PrimitiveUType> decompose() {
        HashSet<PrimitiveUType> result = new HashSet<PrimitiveUType>();
        for (PrimitiveUType p : PrimitiveUType.values()) {
            if ((this.bits & 1 << p.getBit()) == 0) continue;
            result.add(p);
        }
        return result;
    }

    public String toString() {
        Set<PrimitiveUType> components = this.decompose();
        if (components.isEmpty()) {
            return "U{}";
        }
        FastStringBuffer sb = new FastStringBuffer(256);
        Iterator<PrimitiveUType> iter = components.iterator();
        boolean started = false;
        while (iter.hasNext()) {
            if (started) {
                sb.append("|");
            }
            started = true;
            sb.append(iter.next().toString());
        }
        return sb.toString();
    }

    public String toStringWithIndefiniteArticle() {
        String s = this.toString();
        if ("aeiouxy".indexOf(s.charAt(0)) >= 0) {
            return "an " + s + " node";
        }
        return "a " + s + " node";
    }

    public boolean overlaps(UType other) {
        return (this.bits & other.bits) != 0;
    }

    public boolean subsumes(UType other) {
        return (this.bits & other.bits) == other.bits;
    }

    public ItemType toItemType() {
        Set<PrimitiveUType> p = this.decompose();
        if (p.isEmpty()) {
            return ErrorType.getInstance();
        }
        if (p.size() == 1) {
            return p.toArray(new PrimitiveUType[1])[0].toItemType();
        }
        if (ANY_NODE.subsumes(this)) {
            return AnyNodeTest.getInstance();
        }
        if (this.equals(NUMERIC)) {
            return NumericType.getInstance();
        }
        if (ANY_ATOMIC.subsumes(this)) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        return AnyItemType.getInstance();
    }

    public boolean matches(Item item) {
        return this.subsumes(UType.getUType(item));
    }

    public static UType getUType(Item item) {
        if (item instanceof NodeInfo) {
            return UType.fromTypeCode(((NodeInfo)item).getNodeKind());
        }
        if (item instanceof AtomicValue) {
            return ((AtomicValue)item).getUType();
        }
        if (item instanceof Function) {
            return FUNCTION;
        }
        if (item instanceof ObjectValue) {
            return EXTENSION;
        }
        return VOID;
    }

    public static UType getUType(GroundedValue sequence) {
        Item item;
        UnfailingIterator iter = sequence.iterate();
        UType u = VOID;
        while ((item = iter.next()) != null) {
            u = u.union(UType.getUType(item));
        }
        return u;
    }

    public static boolean isPossiblyComparable(UType t1, UType t2, boolean ordered) {
        if (t1 == t2) {
            return true;
        }
        if (t1 == ANY_ATOMIC || t2 == ANY_ATOMIC) {
            return true;
        }
        if (t1 == UNTYPED_ATOMIC || t1 == ANY_URI) {
            t1 = STRING;
        }
        if (t2 == UNTYPED_ATOMIC || t2 == ANY_URI) {
            t2 = STRING;
        }
        if (NUMERIC.subsumes(t1)) {
            t1 = NUMERIC;
        }
        if (NUMERIC.subsumes(t2)) {
            t2 = NUMERIC;
        }
        return t1 == t2;
    }

    public static boolean isGuaranteedComparable(UType t1, UType t2) {
        if (t1 == t2) {
            return true;
        }
        if (t1 == UNTYPED_ATOMIC || t1 == ANY_URI) {
            t1 = STRING;
        }
        if (t2 == UNTYPED_ATOMIC || t2 == ANY_URI) {
            t2 = STRING;
        }
        if (NUMERIC.subsumes(t1)) {
            t1 = NUMERIC;
        }
        if (NUMERIC.subsumes(t2)) {
            t2 = NUMERIC;
        }
        return t1.equals(t2);
    }

    public static boolean isGenerallyComparable(UType t1, UType t2) {
        return t1 == UNTYPED_ATOMIC || t2 == UNTYPED_ATOMIC || UType.isGuaranteedComparable(t1, t2);
    }
}

