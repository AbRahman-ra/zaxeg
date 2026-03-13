/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.function.Function;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.wrapper.AbstractVirtualNode;
import net.sf.saxon.tree.wrapper.RebasedDocument;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.tree.wrapper.WrappingFunction;
import net.sf.saxon.tree.wrapper.WrappingIterator;

public class RebasedNode
extends AbstractVirtualNode
implements WrappingFunction {
    protected RebasedNode() {
    }

    protected RebasedNode(NodeInfo node, RebasedNode parent) {
        this.node = node;
        this.parent = parent;
    }

    public static RebasedNode makeWrapper(NodeInfo node, RebasedDocument docWrapper, RebasedNode parent) {
        RebasedNode wrapper = new RebasedNode(node, parent);
        wrapper.docWrapper = docWrapper;
        return wrapper;
    }

    @Override
    public RebasedNode makeWrapper(NodeInfo node, VirtualNode parent) {
        RebasedNode wrapper = new RebasedNode(node, (RebasedNode)parent);
        wrapper.docWrapper = this.docWrapper;
        return wrapper;
    }

    private Function<NodeInfo, String> getBaseUriMappingFunction() {
        return ((RebasedDocument)this.docWrapper).getBaseUriMapper();
    }

    private Function<NodeInfo, String> getSystemIdMappingFunction() {
        return ((RebasedDocument)this.docWrapper).getSystemIdMapper();
    }

    @Override
    public String getBaseURI() {
        return this.getBaseUriMappingFunction().apply(this.node);
    }

    @Override
    public String getSystemId() {
        return this.getSystemIdMappingFunction().apply(this.node);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RebasedNode && this.node.equals(((RebasedNode)other).node);
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof RebasedNode) {
            return this.node.compareOrder(((RebasedNode)other).node);
        }
        return this.node.compareOrder(other);
    }

    @Override
    public NodeInfo getParent() {
        NodeInfo realParent;
        if (this.parent == null && (realParent = this.node.getParent()) != null) {
            this.parent = RebasedNode.makeWrapper(realParent, (RebasedDocument)this.docWrapper, null);
        }
        return this.parent;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        return new WrappingIterator(this.node.iterateAxis(axisNumber), this, null);
    }
}

