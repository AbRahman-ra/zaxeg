/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Cardinality;

public abstract class StaticProperty {
    public static final int DEPENDS_ON_CURRENT_ITEM = 1;
    public static final int DEPENDS_ON_CONTEXT_ITEM = 2;
    public static final int DEPENDS_ON_POSITION = 4;
    public static final int DEPENDS_ON_LAST = 8;
    public static final int DEPENDS_ON_CONTEXT_DOCUMENT = 16;
    public static final int DEPENDS_ON_CURRENT_GROUP = 32;
    public static final int DEPENDS_ON_REGEX_GROUP = 64;
    public static final int DEPENDS_ON_LOCAL_VARIABLES = 128;
    public static final int DEPENDS_ON_USER_FUNCTIONS = 256;
    public static final int DEPENDS_ON_ASSIGNABLE_GLOBALS = 512;
    public static final int DEPENDS_ON_RUNTIME_ENVIRONMENT = 1024;
    public static final int DEPENDS_ON_STATIC_CONTEXT = 2048;
    public static final int DEPENDS_ON_XSLT_CONTEXT = 609;
    public static final int DEPENDS_ON_FOCUS = 30;
    public static final int DEPENDS_ON_NON_DOCUMENT_FOCUS = 14;
    public static final int ALLOWS_ZERO = 8192;
    public static final int ALLOWS_ONE = 16384;
    public static final int ALLOWS_MANY = 32768;
    public static final int CARDINALITY_MASK = 57344;
    public static final int ALLOWS_ONE_OR_MORE = 49152;
    public static final int ALLOWS_ZERO_OR_MORE = 57344;
    public static final int ALLOWS_ZERO_OR_ONE = 24576;
    public static final int EXACTLY_ONE = 16384;
    public static final int EMPTY = 8192;
    public static final int CONTEXT_DOCUMENT_NODESET = 65536;
    public static final int ORDERED_NODESET = 131072;
    public static final int REVERSE_DOCUMENT_ORDER = 262144;
    public static final int PEER_NODESET = 524288;
    public static final int SUBTREE_NODESET = 0x100000;
    public static final int ATTRIBUTE_NS_NODESET = 0x200000;
    public static final int ALL_NODES_NEWLY_CREATED = 0x400000;
    public static final int NO_NODES_NEWLY_CREATED = 0x800000;
    public static final int SINGLE_DOCUMENT_NODESET = 0x1000000;
    public static final int HAS_SIDE_EFFECTS = 0x2000000;
    public static final int NOT_UNTYPED_ATOMIC = 0x4000000;
    public static final int ALL_NODES_UNTYPED = 0x8000000;
    public static final int DEPENDENCY_MASK = 0x2000FFF;
    public static final int SPECIAL_PROPERTY_MASK = 0xFFF0000;
    public static final int NODESET_PROPERTIES = 155123712;

    public static int getCardinalityCode(int cardinality) {
        return (cardinality & 0xE000) >> 13;
    }

    private StaticProperty() {
    }

    public static String display(int props) {
        FastStringBuffer s = new FastStringBuffer(128);
        s.append("D(");
        if ((props & 1) != 0) {
            s.append("U");
        }
        if ((props & 2) != 0) {
            s.append("C");
        }
        if ((props & 4) != 0) {
            s.append("P");
        }
        if ((props & 8) != 0) {
            s.append("L");
        }
        if ((props & 0x10) != 0) {
            s.append("D");
        }
        if ((props & 0x80) != 0) {
            s.append("V");
        }
        if ((props & 0x200) != 0) {
            s.append("A");
        }
        if ((props & 0x40) != 0) {
            s.append("R");
        }
        if ((props & 0x400) != 0) {
            s.append("E");
        }
        if ((props & 0x800) != 0) {
            s.append("S");
        }
        s.append(") C(");
        boolean m = Cardinality.allowsMany(props);
        boolean z = Cardinality.allowsZero(props);
        if (m && z) {
            s.append("*");
        } else if (m) {
            s.append("+");
        } else if (z) {
            s.append("?");
        } else {
            s.append("1");
        }
        s.append(") S(");
        if ((props & 0x2000000) != 0) {
            s.append("E");
        }
        if ((props & 0x800000) != 0) {
            s.append("N");
        }
        if ((props & 0x4000000) != 0) {
            s.append("T");
        }
        if ((props & 0x20000) != 0) {
            s.append("O");
        }
        if ((props & 0x80000) != 0) {
            s.append("P");
        }
        if ((props & 0x40000) != 0) {
            s.append("R");
        }
        if ((props & 0x1000000) != 0) {
            s.append("S");
        }
        if ((props & 0x100000) != 0) {
            s.append("D");
        }
        s.append(")");
        return s.toString();
    }
}

