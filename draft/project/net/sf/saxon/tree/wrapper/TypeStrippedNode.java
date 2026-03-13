/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.wrapper.AbstractVirtualNode;
import net.sf.saxon.tree.wrapper.TypeStrippedDocument;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.tree.wrapper.WrappingFunction;
import net.sf.saxon.tree.wrapper.WrappingIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.UntypedAtomicValue;

public class TypeStrippedNode
extends AbstractVirtualNode
implements WrappingFunction {
    protected TypeStrippedNode() {
    }

    protected TypeStrippedNode(NodeInfo node, TypeStrippedNode parent) {
        this.node = node;
        this.parent = parent;
    }

    public static TypeStrippedNode makeWrapper(NodeInfo node, TypeStrippedDocument docWrapper, TypeStrippedNode parent) {
        TypeStrippedNode wrapper = new TypeStrippedNode(node, parent);
        wrapper.docWrapper = docWrapper;
        return wrapper;
    }

    @Override
    public VirtualNode makeWrapper(NodeInfo node, VirtualNode parent) {
        TypeStrippedNode wrapper = new TypeStrippedNode(node, (TypeStrippedNode)parent);
        wrapper.docWrapper = this.docWrapper;
        return wrapper;
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return new UntypedAtomicValue(this.getStringValueCS());
    }

    @Override
    public SchemaType getSchemaType() {
        if (this.getNodeKind() == 1) {
            return Untyped.getInstance();
        }
        return BuiltInAtomicType.UNTYPED_ATOMIC;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TypeStrippedNode) {
            return this.node.equals(((TypeStrippedNode)other).node);
        }
        return this.node.equals(other);
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof TypeStrippedNode) {
            return this.node.compareOrder(((TypeStrippedNode)other).node);
        }
        return this.node.compareOrder(other);
    }

    @Override
    public NodeInfo getParent() {
        NodeInfo realParent;
        if (this.parent == null && (realParent = this.node.getParent()) != null) {
            this.parent = TypeStrippedNode.makeWrapper(realParent, (TypeStrippedDocument)this.docWrapper, null);
        }
        return this.parent;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        return new WrappingIterator(this.node.iterateAxis(axisNumber), this, null);
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        this.node.copy(out, copyOptions & 0xFFFFFFFB, locationId);
    }

    @Override
    public boolean isNilled() {
        return false;
    }
}

