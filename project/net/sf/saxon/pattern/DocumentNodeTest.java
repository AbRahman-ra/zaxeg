/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.Optional;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.Err;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public class DocumentNodeTest
extends NodeTest {
    private NodeTest elementTest;

    public DocumentNodeTest(NodeTest elementTest) {
        this.elementTest = elementTest;
    }

    @Override
    public UType getUType() {
        return UType.DOCUMENT;
    }

    @Override
    public boolean matches(int nodeKind, NodeName name, SchemaType annotation) {
        if (nodeKind != 9) {
            return false;
        }
        throw new UnsupportedOperationException("DocumentNodeTest doesn't support this method");
    }

    @Override
    public boolean test(NodeInfo node) {
        NodeInfo n;
        if (node.getNodeKind() != 9) {
            return false;
        }
        AxisIterator iter = node.iterateAxis(3);
        boolean found = false;
        while ((n = iter.next()) != null) {
            int kind = n.getNodeKind();
            if (kind == 3) {
                return false;
            }
            if (kind != 1) continue;
            if (found) {
                return false;
            }
            if (this.elementTest.test(n)) {
                found = true;
                continue;
            }
            return false;
        }
        return found;
    }

    @Override
    public final double getDefaultPriority() {
        return this.elementTest.getDefaultPriority();
    }

    @Override
    public int getPrimitiveType() {
        return 9;
    }

    public NodeTest getElementTest() {
        return this.elementTest;
    }

    @Override
    public String toString() {
        return "document-node(" + this.elementTest + ')';
    }

    public int hashCode() {
        return this.elementTest.hashCode() ^ 0x3039;
    }

    public boolean equals(Object other) {
        return other instanceof DocumentNodeTest && ((DocumentNodeTest)other).elementTest.equals(this.elementTest);
    }

    @Override
    public String getFullAlphaCode() {
        return this.getBasicAlphaCode() + " e[" + this.elementTest.getFullAlphaCode() + "]";
    }

    @Override
    public Optional<String> explainMismatch(Item item, TypeHierarchy th) {
        NodeInfo n;
        Optional<String> explanation = super.explainMismatch(item, th);
        if (explanation.isPresent()) {
            return explanation;
        }
        NodeInfo node = (NodeInfo)item;
        AxisIterator iter = node.iterateAxis(3);
        boolean found = false;
        while ((n = iter.next()) != null) {
            int kind = n.getNodeKind();
            if (kind == 3) {
                return Optional.of("The supplied document node has text node children");
            }
            if (kind != 1) continue;
            if (found) {
                return Optional.of("The supplied document node has more than one element child");
            }
            if (this.elementTest.test(n)) {
                found = true;
                continue;
            }
            String s = "The supplied document node has an element child (" + Err.depict(n) + ") that does not satisfy the element test";
            Optional<String> more = this.elementTest.explainMismatch(n, th);
            if (more.isPresent()) {
                s = s + ". " + more.get();
            }
            return Optional.of(s);
        }
        return Optional.empty();
    }
}

