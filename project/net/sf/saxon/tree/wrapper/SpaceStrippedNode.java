/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.function.Predicate;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.wrapper.AbstractVirtualNode;
import net.sf.saxon.tree.wrapper.SpaceStrippedDocument;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.tree.wrapper.WrappingFunction;
import net.sf.saxon.tree.wrapper.WrappingIterator;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Whitespace;

public class SpaceStrippedNode
extends AbstractVirtualNode
implements WrappingFunction {
    protected SpaceStrippedNode() {
    }

    protected SpaceStrippedNode(NodeInfo node, SpaceStrippedNode parent) {
        this.node = node;
        this.parent = parent;
    }

    protected static SpaceStrippedNode makeWrapper(NodeInfo node, SpaceStrippedDocument docWrapper, SpaceStrippedNode parent) {
        SpaceStrippedNode wrapper = new SpaceStrippedNode(node, parent);
        wrapper.docWrapper = docWrapper;
        return wrapper;
    }

    @Override
    public VirtualNode makeWrapper(NodeInfo node, VirtualNode parent) {
        SpaceStrippedNode wrapper = new SpaceStrippedNode(node, (SpaceStrippedNode)parent);
        wrapper.docWrapper = this.docWrapper;
        return wrapper;
    }

    public static boolean isPreservedNode(NodeInfo node, SpaceStrippedDocument docWrapper, NodeInfo actualParent) {
        NodeInfo p;
        if (node.getNodeKind() != 3 || actualParent == null || !Whitespace.isWhite(node.getStringValueCS())) {
            return true;
        }
        SchemaType type = actualParent.getSchemaType();
        if (type.isSimpleType() || ((ComplexType)type).isSimpleContent()) {
            return true;
        }
        if (docWrapper.containsPreserveSpace()) {
            p = actualParent;
            while (p.getNodeKind() == 1) {
                String val = p.getAttributeValue("http://www.w3.org/XML/1998/namespace", "space");
                if (val != null) {
                    if ("preserve".equals(val)) {
                        return true;
                    }
                    if ("default".equals(val)) break;
                }
                p = p.getParent();
            }
        }
        if (docWrapper.containsAssertions()) {
            p = actualParent;
            while (p.getNodeKind() == 1) {
                SchemaType t = p.getSchemaType();
                if (t instanceof ComplexType && ((ComplexType)t).hasAssertions()) {
                    return true;
                }
                p = p.getParent();
            }
        }
        try {
            int preserve = docWrapper.getStrippingRule().isSpacePreserving(NameOfNode.makeName(actualParent), null);
            return preserve == 1;
        } catch (XPathException e) {
            return true;
        }
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        if (this.getNodeKind() == 1) {
            return this.getSchemaType().atomize(this);
        }
        return this.node.atomize();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SpaceStrippedNode) {
            return this.node.equals(((SpaceStrippedNode)other).node);
        }
        return this.node.equals(other);
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof SpaceStrippedNode) {
            return this.node.compareOrder(((SpaceStrippedNode)other).node);
        }
        return this.node.compareOrder(other);
    }

    @Override
    public CharSequence getStringValueCS() {
        switch (this.getNodeKind()) {
            case 1: 
            case 9: {
                NodeInfo it;
                AxisIterator iter = this.iterateAxis(4, NodeKindTest.makeNodeKindTest(3));
                FastStringBuffer sb = new FastStringBuffer(64);
                while ((it = iter.next()) != null) {
                    sb.cat(it.getStringValueCS());
                }
                return sb.condense();
            }
        }
        return this.node.getStringValueCS();
    }

    @Override
    public NodeInfo getParent() {
        NodeInfo realParent;
        if (this.parent == null && (realParent = this.node.getParent()) != null) {
            this.parent = SpaceStrippedNode.makeWrapper(realParent, (SpaceStrippedDocument)this.docWrapper, null);
        }
        return this.parent;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        if (nodeTest instanceof NodeTest && ((NodeTest)nodeTest).getUType().intersection(UType.TEXT) == UType.VOID || axisNumber == 2 || axisNumber == 8) {
            return new WrappingIterator(this.node.iterateAxis(axisNumber, nodeTest), this, this.getParentForAxis(axisNumber));
        }
        return new StrippingIterator(this.node.iterateAxis(axisNumber, nodeTest), this.getParentForAxis(axisNumber));
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        switch (axisNumber) {
            case 2: 
            case 8: {
                return new WrappingIterator(this.node.iterateAxis(axisNumber), this, this);
            }
            case 3: {
                return new StrippingIterator(this.node.iterateAxis(axisNumber), this);
            }
            case 7: 
            case 11: {
                SpaceStrippedNode parent = (SpaceStrippedNode)this.getParent();
                if (parent == null) {
                    return EmptyIterator.ofNodes();
                }
                return new StrippingIterator(this.node.iterateAxis(axisNumber), parent);
            }
        }
        return new StrippingIterator(this.node.iterateAxis(axisNumber), null);
    }

    private SpaceStrippedNode getParentForAxis(int axisNumber) {
        switch (axisNumber) {
            case 2: 
            case 3: 
            case 8: {
                return this;
            }
            case 7: 
            case 11: {
                return (SpaceStrippedNode)this.getParent();
            }
        }
        return null;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        Receiver temp = out;
        Stripper stripper = new Stripper(((SpaceStrippedDocument)this.docWrapper).getStrippingRule(), temp);
        this.node.copy(stripper, copyOptions, locationId);
    }

    private final class StrippingIterator
    implements AxisIterator {
        AxisIterator base;
        SpaceStrippedNode parent;
        NodeInfo currentVirtualNode;
        int position;

        public StrippingIterator(AxisIterator base, SpaceStrippedNode parent) {
            this.base = base;
            this.parent = parent;
            this.position = 0;
        }

        @Override
        public NodeInfo next() {
            NodeInfo nextRealNode;
            do {
                if ((nextRealNode = this.base.next()) != null) continue;
                return null;
            } while (!this.isPreserved(nextRealNode));
            this.currentVirtualNode = SpaceStrippedNode.makeWrapper(nextRealNode, (SpaceStrippedDocument)SpaceStrippedNode.this.docWrapper, this.parent);
            ++this.position;
            return this.currentVirtualNode;
        }

        private boolean isPreserved(NodeInfo nextRealNode) {
            if (nextRealNode.getNodeKind() != 3) {
                return true;
            }
            NodeInfo actualParent = this.parent == null ? nextRealNode.getParent() : this.parent.node;
            return SpaceStrippedNode.isPreservedNode(nextRealNode, (SpaceStrippedDocument)SpaceStrippedNode.this.docWrapper, actualParent);
        }

        @Override
        public void close() {
            this.base.close();
        }
    }
}

