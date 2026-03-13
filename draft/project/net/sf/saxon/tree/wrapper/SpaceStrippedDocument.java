/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.Iterator;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.wrapper.SpaceStrippedNode;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;

public class SpaceStrippedDocument
extends GenericTreeInfo {
    private SpaceStrippingRule strippingRule;
    private boolean preservesSpace;
    private boolean containsAssertions;
    private TreeInfo underlyingTree;

    public SpaceStrippedDocument(TreeInfo doc, SpaceStrippingRule strippingRule) {
        super(doc.getConfiguration());
        this.setRootNode(this.wrap(doc.getRootNode()));
        this.strippingRule = strippingRule;
        this.underlyingTree = doc;
        this.preservesSpace = SpaceStrippedDocument.findPreserveSpace(doc);
        this.containsAssertions = SpaceStrippedDocument.findAssertions(doc);
    }

    public SpaceStrippedNode wrap(NodeInfo node) {
        return SpaceStrippedNode.makeWrapper(node, this, null);
    }

    @Override
    public boolean isTyped() {
        return this.underlyingTree.isTyped();
    }

    public SpaceStrippingRule getStrippingRule() {
        return this.strippingRule;
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        NodeInfo n = this.underlyingTree.selectID(id, false);
        if (n == null) {
            return null;
        }
        return this.wrap(n);
    }

    @Override
    public Iterator<String> getUnparsedEntityNames() {
        return this.underlyingTree.getUnparsedEntityNames();
    }

    @Override
    public String[] getUnparsedEntity(String name) {
        return this.underlyingTree.getUnparsedEntity(name);
    }

    private static boolean findPreserveSpace(TreeInfo doc) {
        NodeInfo node;
        if (doc instanceof TinyTree) {
            return ((TinyTree)doc).hasXmlSpacePreserveAttribute();
        }
        AxisIterator iter = doc.getRootNode().iterateAxis(4, NodeKindTest.ELEMENT);
        while ((node = iter.next()) != null) {
            String val = node.getAttributeValue("http://www.w3.org/XML/1998/namespace", "space");
            if (!"preserve".equals(val)) continue;
            return true;
        }
        return false;
    }

    private static boolean findAssertions(TreeInfo doc) {
        if (doc.isTyped()) {
            NodeInfo node;
            SchemaType type;
            AxisIterator iter = doc.getRootNode().iterateAxis(4, NodeKindTest.ELEMENT);
            do {
                if ((node = iter.next()) != null) continue;
                return false;
            } while (!(type = node.getSchemaType()).isComplexType() || !((ComplexType)type).hasAssertions());
            return true;
        }
        return false;
    }

    public boolean containsPreserveSpace() {
        return this.preservesSpace;
    }

    public boolean containsAssertions() {
        return this.containsAssertions;
    }
}

