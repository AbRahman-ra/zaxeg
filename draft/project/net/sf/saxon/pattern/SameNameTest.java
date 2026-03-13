/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import java.util.function.IntPredicate;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.tree.tiny.NodeVectorTree;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSingletonSet;

public class SameNameTest
extends NodeTest
implements QNameTest {
    private NodeInfo origin;

    public SameNameTest(NodeInfo origin) {
        this.origin = origin;
    }

    public int getNodeKind() {
        return this.origin.getNodeKind();
    }

    @Override
    public UType getUType() {
        return UType.fromTypeCode(this.origin.getNodeKind());
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        if (nodeKind != this.origin.getNodeKind()) {
            return false;
        }
        if (name.hasFingerprint() && this.origin.hasFingerprint()) {
            return name.getFingerprint() == this.origin.getFingerprint();
        }
        return name.hasURI(this.origin.getURI()) && name.getLocalPart().equals(this.origin.getLocalPart());
    }

    @Override
    public IntPredicate getMatcher(NodeVectorTree tree) {
        byte[] nodeKindArray = tree.getNodeKindArray();
        int[] nameCodeArray = tree.getNameCodeArray();
        return nodeNr -> {
            int k = nodeKindArray[nodeNr] & 0xF;
            if (k == 4) {
                k = 3;
            }
            if (k != this.origin.getNodeKind()) {
                return false;
            }
            if (this.origin.hasFingerprint()) {
                return (nameCodeArray[nodeNr] & 0xFFFFF) == this.origin.getFingerprint();
            }
            return Navigator.haveSameName(tree.getNode(nodeNr), this.origin);
        };
    }

    @Override
    public boolean test(NodeInfo node) {
        return node == this.origin || node.getNodeKind() == this.origin.getNodeKind() && Navigator.haveSameName(node, this.origin);
    }

    @Override
    public boolean matches(StructuredQName qname) {
        return NameOfNode.makeName(this.origin).getStructuredQName().equals(qname);
    }

    @Override
    public final double getDefaultPriority() {
        return 0.0;
    }

    @Override
    public int getFingerprint() {
        if (this.origin.hasFingerprint()) {
            return this.origin.getFingerprint();
        }
        NamePool pool = this.origin.getConfiguration().getNamePool();
        return pool.allocateFingerprint(this.origin.getURI(), this.origin.getLocalPart());
    }

    @Override
    public int getPrimitiveType() {
        return this.origin.getNodeKind();
    }

    @Override
    public Optional<IntSet> getRequiredNodeNames() {
        return Optional.of(new IntSingletonSet(this.getFingerprint()));
    }

    public String getNamespaceURI() {
        return this.origin.getURI();
    }

    public String getLocalPart() {
        return this.origin.getLocalPart();
    }

    @Override
    public String toString() {
        switch (this.origin.getNodeKind()) {
            case 1: {
                return "element(" + NameOfNode.makeName(this.origin).getStructuredQName().getEQName() + ")";
            }
            case 2: {
                return "attribute(" + NameOfNode.makeName(this.origin).getStructuredQName().getEQName() + ")";
            }
            case 7: {
                return "processing-instruction(" + this.origin.getLocalPart() + ')';
            }
            case 13: {
                return "namespace-node(" + this.origin.getLocalPart() + ')';
            }
            case 8: {
                return "comment()";
            }
            case 9: {
                return "document-node()";
            }
            case 3: {
                return "text()";
            }
        }
        return "***";
    }

    public int hashCode() {
        return this.origin.getNodeKind() << 20 ^ this.origin.getURI().hashCode() ^ this.origin.getLocalPart().hashCode();
    }

    public boolean equals(Object other) {
        return other instanceof SameNameTest && this.test(((SameNameTest)other).origin);
    }

    public NameTest getEquivalentNameTest() {
        return new NameTest(this.origin.getNodeKind(), this.origin.getURI(), this.origin.getLocalPart(), this.origin.getConfiguration().getNamePool());
    }

    @Override
    public String exportQNameTest() {
        return "";
    }

    @Override
    public String generateJavaScriptNameTest(int targetVersion) {
        return "false";
    }
}

