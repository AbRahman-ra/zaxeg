/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.tree.wrapper.TypeStrippedNode;

public class TypeStrippedDocument
extends GenericTreeInfo {
    TreeInfo underlyingTree;

    public TypeStrippedDocument(TreeInfo doc) {
        super(doc.getConfiguration());
        this.setRootNode(this.wrap(doc.getRootNode()));
        this.underlyingTree = doc;
    }

    public TypeStrippedNode wrap(NodeInfo node) {
        return TypeStrippedNode.makeWrapper(node, this, null);
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        NodeInfo n = this.underlyingTree.selectID(id, false);
        if (n == null) {
            return null;
        }
        return this.wrap(n);
    }
}

