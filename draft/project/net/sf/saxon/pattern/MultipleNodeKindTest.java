/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.IntPredicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PrimitiveUType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;

public final class MultipleNodeKindTest
extends NodeTest {
    public static final MultipleNodeKindTest PARENT_NODE = new MultipleNodeKindTest(UType.DOCUMENT.union(UType.ELEMENT));
    public static final MultipleNodeKindTest DOC_ELEM_ATTR = new MultipleNodeKindTest(UType.DOCUMENT.union(UType.ELEMENT).union(UType.ATTRIBUTE));
    public static final MultipleNodeKindTest LEAF = new MultipleNodeKindTest(UType.TEXT.union(UType.COMMENT).union(UType.PI).union(UType.NAMESPACE).union(UType.ATTRIBUTE));
    public static final MultipleNodeKindTest CHILD_NODE = new MultipleNodeKindTest(UType.ELEMENT.union(UType.TEXT).union(UType.COMMENT).union(UType.PI));
    UType uType;
    int nodeKindMask;

    public MultipleNodeKindTest(UType u) {
        this.uType = u;
        if (UType.DOCUMENT.overlaps(u)) {
            this.nodeKindMask |= 0x200;
        }
        if (UType.ELEMENT.overlaps(u)) {
            this.nodeKindMask |= 2;
        }
        if (UType.ATTRIBUTE.overlaps(u)) {
            this.nodeKindMask |= 4;
        }
        if (UType.TEXT.overlaps(u)) {
            this.nodeKindMask |= 8;
        }
        if (UType.COMMENT.overlaps(u)) {
            this.nodeKindMask |= 0x100;
        }
        if (UType.PI.overlaps(u)) {
            this.nodeKindMask |= 0x80;
        }
        if (UType.NAMESPACE.overlaps(u)) {
            this.nodeKindMask |= 0x2000;
        }
    }

    @Override
    public UType getUType() {
        return this.uType;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return (this.nodeKindMask & 1 << nodeKind) != 0;
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        return nodeNr -> {
            int nodeKind = nodeKindArray[nodeNr] & 0xF;
            if (nodeKind == 4) {
                nodeKind = 3;
            }
            return (this.nodeKindMask & 1 << nodeKind) != 0;
        };
    }

    @Override
    public boolean test(NodeInfo node) {
        int nodeKind = node.getNodeKind();
        return (this.nodeKindMask & 1 << nodeKind) != 0;
    }

    @Override
    public double getDefaultPriority() {
        return -0.5;
    }

    @Override
    public String toString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        LinkedList<PrimitiveUType> types = new LinkedList<PrimitiveUType>(this.uType.decompose());
        this.format(types, fsb, ItemType::toString);
        return fsb.toString();
    }

    @Override
    public String toExportString() {
        FastStringBuffer fsb = new FastStringBuffer(64);
        LinkedList<PrimitiveUType> types = new LinkedList<PrimitiveUType>(this.uType.decompose());
        this.format(types, fsb, ItemType::toExportString);
        return fsb.toString();
    }

    @Override
    public String toShortString() {
        if (this.nodeKindMask == MultipleNodeKindTest.CHILD_NODE.nodeKindMask) {
            return "node()";
        }
        FastStringBuffer fsb = new FastStringBuffer(64);
        LinkedList<PrimitiveUType> types = new LinkedList<PrimitiveUType>(this.uType.decompose());
        this.format(types, fsb, it -> ((NodeKindTest)it).toShortString());
        return fsb.toString();
    }

    private void format(LinkedList<PrimitiveUType> list, FastStringBuffer fsb, Function<ItemType, String> show) {
        if (list.size() == 1) {
            fsb.append(list.get(0).toItemType().toString());
        } else {
            boolean first = true;
            for (PrimitiveUType pu : list) {
                fsb.cat(first ? (char)'(' : '|');
                first = false;
                fsb.append(((NodeKindTest)pu.toItemType()).toShortString());
            }
            fsb.cat(')');
        }
    }

    public int hashCode() {
        return this.uType.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof MultipleNodeKindTest && this.uType.equals(((MultipleNodeKindTest)obj).uType);
    }
}

