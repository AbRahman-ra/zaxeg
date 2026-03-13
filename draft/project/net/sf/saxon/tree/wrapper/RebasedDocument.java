/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.function.Function;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.tree.wrapper.RebasedNode;

public class RebasedDocument
extends GenericTreeInfo {
    private TreeInfo underlyingTree;
    private Function<NodeInfo, String> baseUriMapper;
    private Function<NodeInfo, String> systemIdMapper;

    public RebasedDocument(TreeInfo doc, Function<NodeInfo, String> baseUriMapper, Function<NodeInfo, String> systemIdMapper) {
        super(doc.getConfiguration());
        this.baseUriMapper = baseUriMapper;
        this.systemIdMapper = systemIdMapper;
        this.setRootNode(this.wrap(doc.getRootNode()));
        this.underlyingTree = doc;
    }

    public RebasedNode wrap(NodeInfo node) {
        return RebasedNode.makeWrapper(node, this, null);
    }

    @Override
    public boolean isTyped() {
        return this.underlyingTree.isTyped();
    }

    @Override
    public NodeInfo selectID(String id, boolean getParent) {
        NodeInfo n = this.underlyingTree.selectID(id, false);
        if (n == null) {
            return null;
        }
        return this.wrap(n);
    }

    public TreeInfo getUnderlyingTree() {
        return this.underlyingTree;
    }

    public Function<NodeInfo, String> getBaseUriMapper() {
        return this.baseUriMapper;
    }

    public Function<NodeInfo, String> getSystemIdMapper() {
        return this.systemIdMapper;
    }
}

