/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.z.IntSet;

public final class NamespaceTest
extends NodeTest
implements QNameTest {
    private NamePool namePool;
    private int nodeKind;
    private UType uType;
    private String uri;

    public NamespaceTest(NamePool pool, int nodeKind, String uri) {
        this.namePool = pool;
        this.nodeKind = nodeKind;
        this.uri = uri;
        this.uType = UType.fromTypeCode(nodeKind);
    }

    public int getNodeKind() {
        return this.nodeKind;
    }

    @Override
    public UType getUType() {
        return this.uType;
    }

    @Override
    public Optional<IntSet> getRequiredNodeNames() {
        return Optional.empty();
    }

    @Override
    public String getFullAlphaCode() {
        return this.getBasicAlphaCode() + " nQ{" + this.uri + "}*";
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return name != null && name.hasURI(this.uri);
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        int[] nameCodeArray = tree.getNameCodeArray();
        return nodeNr -> {
            int fp = nameCodeArray[nodeNr] & 0xFFFFF;
            return fp != -1 && (nodeKindArray[nodeNr] & 0xF) == this.nodeKind && this.uri.equals(this.namePool.getURI(fp));
        };
    }

    @Override
    public boolean test(NodeInfo node) {
        return node.getNodeKind() == this.nodeKind && node.getURI().equals(this.uri);
    }

    @Override
    public boolean matches(StructuredQName qname) {
        return qname.hasURI(this.uri);
    }

    @Override
    public final double getDefaultPriority() {
        return -0.25;
    }

    @Override
    public int getPrimitiveType() {
        return this.nodeKind;
    }

    public String getNamespaceURI() {
        return this.uri;
    }

    @Override
    public String toString() {
        switch (this.nodeKind) {
            case 1: {
                return "Q{" + this.uri + "}*";
            }
            case 2: {
                return "@Q{" + this.uri + "}*";
            }
        }
        return "(*" + this.nodeKind + "*)Q{" + this.uri + "}*";
    }

    public int hashCode() {
        return this.uri.hashCode() << 5 + this.nodeKind;
    }

    public boolean equals(Object other) {
        return other instanceof NamespaceTest && ((NamespaceTest)other).namePool == this.namePool && ((NamespaceTest)other).nodeKind == this.nodeKind && ((NamespaceTest)other).uri.equals(this.uri);
    }

    @Override
    public String exportQNameTest() {
        return "Q{" + this.uri + "}*";
    }

    @Override
    public String generateJavaScriptNameTest(int targetVersion) {
        return "q.uri==='" + ExpressionPresenter.jsEscape(this.uri) + "'";
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        return Optional.of("The node is in the wrong namespace");
    }
}

