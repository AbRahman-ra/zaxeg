/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.IdentityComparable;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.tree.wrapper.AbstractVirtualNode;

public class NameOfNode
implements NodeName {
    private NodeInfo node;

    private NameOfNode(NodeInfo node) {
        this.node = node;
    }

    public static NodeName makeName(NodeInfo node) {
        if (node instanceof MutableNodeInfo) {
            return new FingerprintedQName(node.getPrefix(), node.getURI(), node.getLocalPart());
        }
        if (node instanceof AbstractVirtualNode) {
            return new NameOfNode(((AbstractVirtualNode)node).getUnderlyingNode());
        }
        return new NameOfNode(node);
    }

    @Override
    public String getPrefix() {
        return this.node.getPrefix();
    }

    @Override
    public String getURI() {
        return this.node.getURI();
    }

    @Override
    public String getLocalPart() {
        return this.node.getLocalPart();
    }

    @Override
    public String getDisplayName() {
        return this.node.getDisplayName();
    }

    @Override
    public StructuredQName getStructuredQName() {
        return new StructuredQName(this.getPrefix(), this.getURI(), this.getLocalPart());
    }

    @Override
    public boolean hasURI(String ns) {
        return this.node.getURI().equals(ns);
    }

    @Override
    public NamespaceBinding getNamespaceBinding() {
        return NamespaceBinding.makeNamespaceBinding(this.getPrefix(), this.getURI());
    }

    @Override
    public boolean hasFingerprint() {
        return this.node.hasFingerprint();
    }

    @Override
    public int getFingerprint() {
        if (this.hasFingerprint()) {
            return this.node.getFingerprint();
        }
        return -1;
    }

    @Override
    public int obtainFingerprint(NamePool namePool) {
        if (this.node.hasFingerprint()) {
            return this.node.getFingerprint();
        }
        return namePool.allocateFingerprint(this.node.getURI(), this.node.getLocalPart());
    }

    public int hashCode() {
        return StructuredQName.computeHashCode(this.getURI(), this.getLocalPart());
    }

    public boolean equals(Object obj) {
        if (obj instanceof NodeName) {
            NodeName n = (NodeName)obj;
            if (this.node.hasFingerprint() && n.hasFingerprint()) {
                return this.node.getFingerprint() == n.getFingerprint();
            }
            return n.getLocalPart().equals(this.node.getLocalPart()) && n.hasURI(this.node.getURI());
        }
        return false;
    }

    @Override
    public boolean isIdentical(IdentityComparable other) {
        return other instanceof NodeName && this.equals(other) && this.getPrefix().equals(((NodeName)other).getPrefix());
    }

    @Override
    public int identityHashCode() {
        return this.hashCode() ^ this.getPrefix().hashCode();
    }
}

