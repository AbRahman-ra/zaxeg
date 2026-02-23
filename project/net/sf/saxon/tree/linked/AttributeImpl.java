/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.ParentNodeImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class AttributeImpl
extends NodeImpl {
    public AttributeImpl(ElementImpl element, int index) {
        this.setRawParent(element);
        this.setSiblingPosition(index);
    }

    private AttributeInfo getAttributeInfo() {
        return this.getRawParent().attributes().itemAt(this.getSiblingPosition());
    }

    @Override
    public NodeName getNodeName() {
        if (this.getRawParent() == null || this.getSiblingPosition() == -1) {
            return null;
        }
        return this.getAttributeInfo().getNodeName();
    }

    @Override
    public int getFingerprint() {
        if (this.getRawParent() == null || this.getSiblingPosition() == -1) {
            return -1;
        }
        return this.getNodeName().obtainFingerprint(this.getNamePool());
    }

    @Override
    public SchemaType getSchemaType() {
        return this.getAttributeInfo().getType();
    }

    @Override
    public boolean isId() {
        return this.getAttributeInfo().isId();
    }

    @Override
    public boolean isIdref() {
        if (ReceiverOption.contains(this.getAttributeInfo().getProperties(), 4096)) {
            return true;
        }
        return ElementImpl.isIdRefNode(this);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AttributeImpl)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        AttributeImpl otherAtt = (AttributeImpl)other;
        return this.getRawParent().equals(otherAtt.getRawParent()) && this.getSiblingPosition() == otherAtt.getSiblingPosition();
    }

    @Override
    public int hashCode() {
        return this.getRawParent().hashCode() ^ this.getSiblingPosition() << 16;
    }

    @Override
    protected long getSequenceNumber() {
        long parseq = this.getRawParent().getSequenceNumber();
        return parseq == -1L ? parseq : parseq + 32768L + (long)this.getSiblingPosition();
    }

    @Override
    public final int getNodeKind() {
        return 2;
    }

    @Override
    public String getStringValue() {
        return this.getAttributeInfo().getValue();
    }

    @Override
    public NodeImpl getNextSibling() {
        return null;
    }

    @Override
    public NodeImpl getPreviousSibling() {
        return null;
    }

    @Override
    public NodeImpl getPreviousInDocument() {
        return this.getParent();
    }

    @Override
    public NodeImpl getNextInDocument(NodeImpl anchor) {
        if (anchor == this) {
            return null;
        }
        return this.getParent().getNextInDocument(anchor);
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        this.getParent().generateId(buffer);
        buffer.cat('a');
        buffer.append(Integer.toString(this.getSiblingPosition()));
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        throw new IllegalArgumentException();
    }

    @Override
    public void delete() {
        if (!this.isDeleted()) {
            if (this.getRawParent() != null) {
                this.getRawParent().removeAttribute(this);
            }
            this.setRawParent(null);
            this.setSiblingPosition(-1);
        }
    }

    @Override
    public boolean isDeleted() {
        return this.getRawParent() == null || this.getAttributeInfo() instanceof AttributeInfo.Deleted || this.getRawParent().isDeleted();
    }

    @Override
    public void replace(NodeInfo[] replacement, boolean inherit) {
        if (this.isDeleted()) {
            throw new IllegalStateException("Cannot replace a deleted node");
        }
        if (this.getParent() == null) {
            throw new IllegalStateException("Cannot replace a parentless node");
        }
        ParentNodeImpl element = this.getRawParent();
        this.delete();
        for (NodeInfo n : replacement) {
            if (n.getNodeKind() != 2) {
                throw new IllegalArgumentException("Replacement nodes must be attributes");
            }
            element.addAttribute(NameOfNode.makeName(n), BuiltInAtomicType.UNTYPED_ATOMIC, n.getStringValue(), 0);
        }
    }

    @Override
    public void rename(NodeName newNameCode) {
        ElementImpl owner = (ElementImpl)this.getRawParent();
        if (owner != null && !this.isDeleted()) {
            AttributeInfo att = this.getAttributeInfo();
            owner.setAttributeInfo(this.getSiblingPosition(), new AttributeInfo(newNameCode, BuiltInAtomicType.UNTYPED_ATOMIC, att.getValue(), att.getLocation(), att.getProperties()));
            String newURI = newNameCode.getURI();
            if (!newURI.isEmpty()) {
                String newPrefix = newNameCode.getPrefix();
                NamespaceBinding newBinding = new NamespaceBinding(newPrefix, newURI);
                String oldURI = ((ElementImpl)this.getRawParent()).getURIForPrefix(newPrefix, false);
                if (oldURI == null) {
                    owner.addNamespace(newBinding);
                } else if (!oldURI.equals(newURI)) {
                    throw new IllegalArgumentException("Namespace binding of new name conflicts with existing namespace binding");
                }
            }
        }
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        ElementImpl owner = (ElementImpl)this.getRawParent();
        if (owner != null && !this.isDeleted()) {
            AttributeInfo att = this.getAttributeInfo();
            owner.setAttributeInfo(this.getSiblingPosition(), new AttributeInfo(att.getNodeName(), att.getType(), stringValue.toString(), att.getLocation(), att.getProperties()));
        }
    }

    @Override
    public void removeTypeAnnotation() {
        ElementImpl owner = (ElementImpl)this.getRawParent();
        if (owner != null && !this.isDeleted()) {
            AttributeInfo att = this.getAttributeInfo();
            owner.setAttributeInfo(this.getSiblingPosition(), new AttributeInfo(att.getNodeName(), BuiltInAtomicType.UNTYPED_ATOMIC, att.getValue(), att.getLocation(), att.getProperties()));
            owner.removeTypeAnnotation();
        }
    }

    @Override
    public void setTypeAnnotation(SchemaType type) {
        if (!(type instanceof SimpleType)) {
            throw new IllegalArgumentException("Attribute type must be a simple type");
        }
        ElementImpl owner = (ElementImpl)this.getRawParent();
        if (owner != null && !this.isDeleted()) {
            AttributeInfo att = this.getAttributeInfo();
            owner.setAttributeInfo(this.getSiblingPosition(), new AttributeInfo(att.getNodeName(), (SimpleType)type, att.getValue(), att.getLocation(), att.getProperties()));
            owner.removeTypeAnnotation();
        }
    }
}

