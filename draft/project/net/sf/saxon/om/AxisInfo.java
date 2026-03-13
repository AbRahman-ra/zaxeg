/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Set;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.PrimitiveUType;
import net.sf.saxon.type.UType;
import net.sf.saxon.z.IntHashMap;

public final class AxisInfo {
    public static final int ANCESTOR = 0;
    public static final int ANCESTOR_OR_SELF = 1;
    public static final int ATTRIBUTE = 2;
    public static final int CHILD = 3;
    public static final int DESCENDANT = 4;
    public static final int DESCENDANT_OR_SELF = 5;
    public static final int FOLLOWING = 6;
    public static final int FOLLOWING_SIBLING = 7;
    public static final int NAMESPACE = 8;
    public static final int PARENT = 9;
    public static final int PRECEDING = 10;
    public static final int PRECEDING_SIBLING = 11;
    public static final int SELF = 12;
    public static final int PRECEDING_OR_ANCESTOR = 13;
    public static final short[] principalNodeType = new short[]{1, 1, 2, 1, 1, 1, 1, 1, 13, 1, 1, 1, 1, 1};
    public static final UType[] principalNodeUType = new UType[]{UType.ELEMENT, UType.ELEMENT, UType.ATTRIBUTE, UType.ELEMENT, UType.ELEMENT, UType.ELEMENT, UType.ELEMENT, UType.ELEMENT, UType.NAMESPACE, UType.ELEMENT, UType.ELEMENT, UType.ELEMENT, UType.ELEMENT, UType.ELEMENT};
    public static final boolean[] isForwards = new boolean[]{false, false, true, true, true, true, true, true, true, true, false, false, true, false};
    public static final boolean[] isPeerAxis = new boolean[]{false, false, true, true, false, false, false, true, true, true, false, true, true, false};
    public static final boolean[] isSubtreeAxis = new boolean[]{false, false, true, true, true, true, false, false, true, false, false, false, true, false};
    public static final String[] axisName = new String[]{"ancestor", "ancestor-or-self", "attribute", "child", "descendant", "descendant-or-self", "following", "following-sibling", "namespace", "parent", "preceding", "preceding-sibling", "self", "preceding-or-ancestor"};
    private static final int DOC = 512;
    private static final int ELE = 2;
    private static final int ATT = 4;
    private static final int TEX = 8;
    private static final int PIN = 128;
    private static final int COM = 256;
    private static final int NAM = 8192;
    private static int[] voidAxisTable = new int[]{512, 0, 9100, 8588, 8588, 0, 512, 8708, 9100, 512, 512, 8708, 0};
    private static int[] nodeKindTable = new int[]{514, 9102, 4, 394, 394, 9102, 394, 394, 8192, 514, 394, 394, 9102};
    public static int[] inverseAxis = new int[]{4, 5, 9, 9, 0, 1, 10, 11, 9, 3, 6, 7, 12};
    public static int[] excludeSelfAxis = new int[]{0, 0, 2, 3, 4, 4, 6, 7, 8, 9, 10, 11, 12};
    private static IntHashMap<UType> axisTransitions = new IntHashMap(50);

    private AxisInfo() {
    }

    public static int getAxisNumber(String name) throws XPathException {
        switch (name) {
            case "ancestor": {
                return 0;
            }
            case "ancestor-or-self": {
                return 1;
            }
            case "attribute": {
                return 2;
            }
            case "child": {
                return 3;
            }
            case "descendant": {
                return 4;
            }
            case "descendant-or-self": {
                return 5;
            }
            case "following": {
                return 6;
            }
            case "following-sibling": {
                return 7;
            }
            case "namespace": {
                return 8;
            }
            case "parent": {
                return 9;
            }
            case "preceding": {
                return 10;
            }
            case "preceding-sibling": {
                return 11;
            }
            case "self": {
                return 12;
            }
            case "preceding-or-ancestor": {
                return 13;
            }
        }
        throw new XPathException("Unknown axis name: " + name);
    }

    public static boolean isAlwaysEmpty(int axis, int nodeKind) {
        return (voidAxisTable[axis] & 1 << nodeKind) != 0;
    }

    public static boolean containsNodeKind(int axis, int nodeKind) {
        return nodeKind == 0 || (nodeKindTable[axis] & 1 << nodeKind) != 0;
    }

    private static void e(PrimitiveUType origin, int axis, UType target) {
        axisTransitions.put(AxisInfo.makeKey(origin, axis), target);
    }

    private static int makeKey(PrimitiveUType origin, int axis) {
        return origin.getBit() << 16 | axis;
    }

    public static UType getTargetUType(UType origin, int axis) {
        UType resultType = UType.VOID;
        Set<PrimitiveUType> origins = origin.intersection(UType.ANY_NODE).decompose();
        for (PrimitiveUType u : origins) {
            UType r = axisTransitions.get(AxisInfo.makeKey(u, axis));
            if (r == null) {
                System.err.println("Unknown transitions for primitive type " + u.toString() + "::" + axis);
            }
            resultType = resultType.union(r);
        }
        return resultType;
    }

    static {
        AxisInfo.e(PrimitiveUType.DOCUMENT, 0, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 1, UType.DOCUMENT);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 2, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 3, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 4, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 5, UType.DOCUMENT.union(UType.CHILD_NODE_KINDS));
        AxisInfo.e(PrimitiveUType.DOCUMENT, 6, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 7, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 8, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 9, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 10, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 11, UType.VOID);
        AxisInfo.e(PrimitiveUType.DOCUMENT, 12, UType.DOCUMENT);
        AxisInfo.e(PrimitiveUType.ELEMENT, 0, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 1, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 2, UType.ATTRIBUTE);
        AxisInfo.e(PrimitiveUType.ELEMENT, 3, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 4, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 5, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 6, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 7, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 8, UType.NAMESPACE);
        AxisInfo.e(PrimitiveUType.ELEMENT, 9, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 10, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 11, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ELEMENT, 12, UType.ELEMENT);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 0, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 1, UType.ATTRIBUTE.union(UType.PARENT_NODE_KINDS));
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 2, UType.VOID);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 3, UType.VOID);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 4, UType.VOID);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 5, UType.ATTRIBUTE);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 6, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 7, UType.VOID);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 8, UType.VOID);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 9, UType.ELEMENT);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 10, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 11, UType.VOID);
        AxisInfo.e(PrimitiveUType.ATTRIBUTE, 12, UType.ATTRIBUTE);
        AxisInfo.e(PrimitiveUType.TEXT, 0, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.TEXT, 1, UType.TEXT.union(UType.PARENT_NODE_KINDS));
        AxisInfo.e(PrimitiveUType.TEXT, 2, UType.VOID);
        AxisInfo.e(PrimitiveUType.TEXT, 3, UType.VOID);
        AxisInfo.e(PrimitiveUType.TEXT, 4, UType.VOID);
        AxisInfo.e(PrimitiveUType.TEXT, 5, UType.TEXT);
        AxisInfo.e(PrimitiveUType.TEXT, 6, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.TEXT, 7, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.TEXT, 8, UType.VOID);
        AxisInfo.e(PrimitiveUType.TEXT, 9, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.TEXT, 10, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.TEXT, 11, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.TEXT, 12, UType.TEXT);
        AxisInfo.e(PrimitiveUType.PI, 0, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.PI, 1, UType.PI.union(UType.PARENT_NODE_KINDS));
        AxisInfo.e(PrimitiveUType.PI, 2, UType.VOID);
        AxisInfo.e(PrimitiveUType.PI, 3, UType.VOID);
        AxisInfo.e(PrimitiveUType.PI, 4, UType.VOID);
        AxisInfo.e(PrimitiveUType.PI, 5, UType.PI);
        AxisInfo.e(PrimitiveUType.PI, 6, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.PI, 7, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.PI, 8, UType.VOID);
        AxisInfo.e(PrimitiveUType.PI, 9, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.PI, 10, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.PI, 11, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.PI, 12, UType.PI);
        AxisInfo.e(PrimitiveUType.COMMENT, 0, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.COMMENT, 1, UType.COMMENT.union(UType.PARENT_NODE_KINDS));
        AxisInfo.e(PrimitiveUType.COMMENT, 2, UType.VOID);
        AxisInfo.e(PrimitiveUType.COMMENT, 3, UType.VOID);
        AxisInfo.e(PrimitiveUType.COMMENT, 4, UType.VOID);
        AxisInfo.e(PrimitiveUType.COMMENT, 5, UType.COMMENT);
        AxisInfo.e(PrimitiveUType.COMMENT, 6, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.COMMENT, 7, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.COMMENT, 8, UType.VOID);
        AxisInfo.e(PrimitiveUType.COMMENT, 9, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.COMMENT, 10, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.COMMENT, 11, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.COMMENT, 12, UType.COMMENT);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 0, UType.PARENT_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 1, UType.NAMESPACE.union(UType.PARENT_NODE_KINDS));
        AxisInfo.e(PrimitiveUType.NAMESPACE, 2, UType.VOID);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 3, UType.VOID);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 4, UType.VOID);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 5, UType.NAMESPACE);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 6, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 7, UType.VOID);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 8, UType.VOID);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 9, UType.ELEMENT);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 10, UType.CHILD_NODE_KINDS);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 11, UType.VOID);
        AxisInfo.e(PrimitiveUType.NAMESPACE, 12, UType.NAMESPACE);
    }
}

