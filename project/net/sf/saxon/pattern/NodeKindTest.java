/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public class NodeKindTest
extends NodeTest {
    public static final NodeKindTest DOCUMENT = new NodeKindTest(9);
    public static final NodeKindTest ELEMENT = new NodeKindTest(1);
    public static final NodeKindTest ATTRIBUTE = new NodeKindTest(2);
    public static final NodeKindTest TEXT = new NodeKindTest(3);
    public static final NodeKindTest COMMENT = new NodeKindTest(8);
    public static final NodeKindTest PROCESSING_INSTRUCTION = new NodeKindTest(7);
    public static final NodeKindTest NAMESPACE = new NodeKindTest(13);
    private int kind;
    private UType uType;

    private NodeKindTest(int nodeKind) {
        this.kind = nodeKind;
        this.uType = UType.fromTypeCode(nodeKind);
    }

    public int getNodeKind() {
        return this.kind;
    }

    @Override
    public UType getUType() {
        return this.uType;
    }

    public static NodeTest makeNodeKindTest(int kind) {
        switch (kind) {
            case 9: {
                return DOCUMENT;
            }
            case 1: {
                return ELEMENT;
            }
            case 2: {
                return ATTRIBUTE;
            }
            case 8: {
                return COMMENT;
            }
            case 3: {
                return TEXT;
            }
            case 7: {
                return PROCESSING_INSTRUCTION;
            }
            case 13: {
                return NAMESPACE;
            }
            case 0: {
                return AnyNodeTest.getInstance();
            }
        }
        throw new IllegalArgumentException("Unknown node kind " + kind + " in NodeKindTest");
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) {
        return item instanceof NodeInfo && this.kind == ((NodeInfo)item).getNodeKind();
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return this.kind == nodeKind;
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        if (this.kind == 3) {
            return nodeNr -> {
                byte k = nodeKindArray[nodeNr];
                return k == 3 || k == 4;
            };
        }
        return nodeNr -> (nodeKindArray[nodeNr] & 0xF) == this.kind;
    }

    @Override
    public boolean test(NodeInfo node) {
        return node.getNodeKind() == this.kind;
    }

    @Override
    public final double getDefaultPriority() {
        return -0.5;
    }

    @Override
    public int getPrimitiveType() {
        return this.kind;
    }

    @Override
    public SchemaType getContentType() {
        switch (this.kind) {
            case 9: {
                return AnyType.getInstance();
            }
            case 1: {
                return AnyType.getInstance();
            }
            case 2: {
                return AnySimpleType.getInstance();
            }
            case 8: {
                return BuiltInAtomicType.STRING;
            }
            case 3: {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
            case 7: {
                return BuiltInAtomicType.STRING;
            }
            case 13: {
                return BuiltInAtomicType.STRING;
            }
        }
        throw new AssertionError((Object)"Unknown node kind");
    }

    @Override
    public AtomicType getAtomizedItemType() {
        switch (this.kind) {
            case 9: {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
            case 1: {
                return BuiltInAtomicType.ANY_ATOMIC;
            }
            case 2: {
                return BuiltInAtomicType.ANY_ATOMIC;
            }
            case 8: {
                return BuiltInAtomicType.STRING;
            }
            case 3: {
                return BuiltInAtomicType.UNTYPED_ATOMIC;
            }
            case 7: {
                return BuiltInAtomicType.STRING;
            }
            case 13: {
                return BuiltInAtomicType.STRING;
            }
        }
        throw new AssertionError((Object)"Unknown node kind");
    }

    @Override
    public String toString() {
        return NodeKindTest.toString(this.kind);
    }

    public static String toString(int kind) {
        switch (kind) {
            case 9: {
                return "document-node()";
            }
            case 1: {
                return "element()";
            }
            case 2: {
                return "attribute()";
            }
            case 8: {
                return "comment()";
            }
            case 3: {
                return "text()";
            }
            case 7: {
                return "processing-instruction()";
            }
            case 13: {
                return "namespace-node()";
            }
        }
        return "** error **";
    }

    public static String nodeKindName(int kind) {
        switch (kind) {
            case 9: {
                return "document";
            }
            case 1: {
                return "element";
            }
            case 2: {
                return "attribute";
            }
            case 8: {
                return "comment";
            }
            case 3: {
                return "text";
            }
            case 7: {
                return "processing-instruction";
            }
            case 13: {
                return "namespace";
            }
        }
        return "** error **";
    }

    public int hashCode() {
        return this.kind;
    }

    public boolean equals(Object other) {
        return other instanceof NodeKindTest && ((NodeKindTest)other).kind == this.kind;
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        if (item instanceof NodeInfo) {
            UType actualKind = UType.getUType(item);
            if (!this.getUType().overlaps(actualKind)) {
                return Optional.of("The supplied value is " + actualKind.toStringWithIndefiniteArticle());
            }
            return Optional.empty();
        }
        return Optional.of("The supplied value is " + item.getGenre().getDescription());
    }

    @Override
    public String toShortString() {
        switch (this.getNodeKind()) {
            case 1: {
                return "*";
            }
            case 2: {
                return "@*";
            }
            case 9: {
                return "/";
            }
        }
        return this.toString();
    }
}

