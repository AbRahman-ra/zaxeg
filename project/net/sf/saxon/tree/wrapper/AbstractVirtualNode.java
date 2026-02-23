/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.function.Predicate;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.type.SchemaType;

public abstract class AbstractVirtualNode
implements VirtualNode {
    protected NodeInfo node;
    protected AbstractVirtualNode parent;
    protected TreeInfo docWrapper;

    @Override
    public TreeInfo getTreeInfo() {
        return this.docWrapper;
    }

    @Override
    public NodeInfo getUnderlyingNode() {
        return this.node;
    }

    @Override
    public int getFingerprint() {
        if (this.node.hasFingerprint()) {
            return this.node.getFingerprint();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasFingerprint() {
        return this.node.hasFingerprint();
    }

    @Override
    public Object getRealNode() {
        Object u = this;
        while ((u = ((VirtualNode)u).getUnderlyingNode()) instanceof VirtualNode) {
        }
        return u;
    }

    @Override
    public int getNodeKind() {
        return this.node.getNodeKind();
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return this.node.atomize();
    }

    @Override
    public SchemaType getSchemaType() {
        return this.node.getSchemaType();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AbstractVirtualNode) {
            return this.node.equals(((AbstractVirtualNode)other).node);
        }
        return this.node.equals(other);
    }

    @Override
    public int hashCode() {
        return this.node.hashCode() ^ 0x3C3C3C3C;
    }

    @Override
    public String getSystemId() {
        return this.node.getSystemId();
    }

    @Override
    public void setSystemId(String uri) {
        this.node.setSystemId(uri);
    }

    @Override
    public String getBaseURI() {
        return this.node.getBaseURI();
    }

    @Override
    public int getLineNumber() {
        return this.node.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return this.node.getColumnNumber();
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof AbstractVirtualNode) {
            return this.node.compareOrder(((AbstractVirtualNode)other).node);
        }
        return this.node.compareOrder(other);
    }

    @Override
    public final String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.node.getStringValueCS();
    }

    @Override
    public String getLocalPart() {
        return this.node.getLocalPart();
    }

    @Override
    public String getURI() {
        return this.node.getURI();
    }

    @Override
    public String getPrefix() {
        return this.node.getPrefix();
    }

    @Override
    public String getDisplayName() {
        return this.node.getDisplayName();
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        return new Navigator.AxisFilter(this.iterateAxis(axisNumber), nodeTest);
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return this.node.getAttributeValue(uri, local);
    }

    @Override
    public NodeInfo getRoot() {
        NodeInfo p = this;
        NodeInfo q;
        while ((q = p.getParent()) != null) {
            p = q;
        }
        return p;
    }

    @Override
    public boolean hasChildNodes() {
        return this.node.hasChildNodes();
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        this.node.generateId(buffer);
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        return this.node.getDeclaredNamespaces(buffer);
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        return this.node.getAllNamespaces();
    }

    @Override
    public boolean isId() {
        return this.node.isId();
    }

    @Override
    public boolean isIdref() {
        return this.node.isIdref();
    }

    @Override
    public boolean isNilled() {
        return this.node.isNilled();
    }
}

