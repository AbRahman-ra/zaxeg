/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.function.IntPredicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;

public final class AnyNodeTest
extends NodeTest
implements QNameTest {
    private static AnyNodeTest THE_INSTANCE = new AnyNodeTest();

    public static AnyNodeTest getInstance() {
        return THE_INSTANCE;
    }

    private AnyNodeTest() {
    }

    @Override
    public UType getUType() {
        return UType.ANY_NODE;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return nodeKind != 12;
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        return nodeNr -> nodeKindArray[nodeNr] != 12;
    }

    @Override
    public boolean test(NodeInfo node) {
        return true;
    }

    @Override
    public boolean matches(StructuredQName qname) {
        return true;
    }

    @Override
    public final double getDefaultPriority() {
        return -0.5;
    }

    @Override
    public String toString() {
        return "node()";
    }

    @Override
    public String exportQNameTest() {
        return "*";
    }

    @Override
    public String generateJavaScriptNameTest(int targetVersion) {
        return "true";
    }
}

