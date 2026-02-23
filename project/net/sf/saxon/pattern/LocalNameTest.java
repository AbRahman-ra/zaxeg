/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Map;
import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.z.IntSet;

public final class LocalNameTest
extends NodeTest
implements QNameTest {
    private NamePool namePool;
    private int nodeKind;
    private String localName;
    private UType uType;

    public LocalNameTest(NamePool pool, int nodeKind, String localName) {
        this.namePool = pool;
        this.nodeKind = nodeKind;
        this.localName = localName;
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
        return this.getBasicAlphaCode() + " n*:" + this.localName;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        return name != null && nodeKind == this.nodeKind && this.localName.equals(name.getLocalPart());
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        int[] nameCodeArray = tree.getNameCodeArray();
        if (this.nodeKind == 1 && tree instanceof TinyTree) {
            Map<String, IntSet> localNameIndex = ((TinyTree)tree).getLocalNameIndex();
            IntSet intSet = localNameIndex.get(this.localName);
            if (intSet == null) {
                return i -> false;
            }
            return nodeNr -> intSet.contains(nameCodeArray[nodeNr] & 0xFFFFF) && (nodeKindArray[nodeNr] & 0xF) == 1;
        }
        return nodeNr -> (nodeKindArray[nodeNr] & 0xF) == this.nodeKind && this.localName.equals(this.namePool.getLocalName(nameCodeArray[nodeNr] & 0xFFFFF));
    }

    @Override
    public boolean test(NodeInfo node) {
        return this.localName.equals(node.getLocalPart()) && this.nodeKind == node.getNodeKind();
    }

    @Override
    public boolean matches(StructuredQName qname) {
        return this.localName.equals(qname.getLocalPart());
    }

    @Override
    public final double getDefaultPriority() {
        return -0.25;
    }

    public String getLocalName() {
        return this.localName;
    }

    @Override
    public int getPrimitiveType() {
        return this.nodeKind;
    }

    @Override
    public String toString() {
        switch (this.nodeKind) {
            case 1: {
                return "*:" + this.localName;
            }
            case 2: {
                return "@*:" + this.localName;
            }
        }
        return "(*" + this.nodeKind + "*):" + this.localName;
    }

    public int hashCode() {
        return this.nodeKind << 20 ^ this.localName.hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof LocalNameTest && ((LocalNameTest)other).nodeKind == this.nodeKind && ((LocalNameTest)other).localName.equals(this.localName);
    }

    public NamePool getNamePool() {
        return this.namePool;
    }

    @Override
    public String exportQNameTest() {
        return "*:" + this.localName;
    }

    @Override
    public String generateJavaScriptNameTest(int targetVersion) {
        return "q.local==='" + this.localName + "'";
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        return Optional.of("The node has the wrong local name");
    }
}

