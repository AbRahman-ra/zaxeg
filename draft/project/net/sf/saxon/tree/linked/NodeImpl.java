/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.linked.AncestorEnumeration;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.FollowingEnumeration;
import net.sf.saxon.tree.linked.FollowingSiblingEnumeration;
import net.sf.saxon.tree.linked.ParentNodeImpl;
import net.sf.saxon.tree.linked.PrecedingEnumeration;
import net.sf.saxon.tree.linked.PrecedingOrAncestorEnumeration;
import net.sf.saxon.tree.linked.PrecedingSiblingEnumeration;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.util.SteppingNavigator;
import net.sf.saxon.tree.util.SteppingNode;
import net.sf.saxon.tree.wrapper.SiblingCountingNode;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.UntypedAtomicValue;

public abstract class NodeImpl
implements MutableNodeInfo,
SteppingNode<NodeImpl>,
SiblingCountingNode,
Location {
    private ParentNodeImpl parent;
    private int index;
    public static final char[] NODE_LETTER = new char[]{'x', 'e', 'a', 't', 'x', 'x', 'x', 'p', 'c', 'r', 'x', 'x', 'x', 'n'};

    @Override
    public NodeImpl head() {
        return this;
    }

    @Override
    public TreeInfo getTreeInfo() {
        return this.getPhysicalRoot();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.getStringValue();
    }

    @Override
    public SchemaType getSchemaType() {
        return Untyped.getInstance();
    }

    @Override
    public int getColumnNumber() {
        if (this.parent == null) {
            return -1;
        }
        return this.parent.getColumnNumber();
    }

    @Override
    public final int getSiblingPosition() {
        return this.index;
    }

    protected final void setSiblingPosition(int index) {
        this.index = index;
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        SchemaType stype = this.getSchemaType();
        if (stype == Untyped.getInstance() || stype == BuiltInAtomicType.UNTYPED_ATOMIC) {
            return new UntypedAtomicValue(this.getStringValueCS());
        }
        return stype.atomize(this);
    }

    @Override
    public void setSystemId(String uri) {
        NodeImpl p = this.getParent();
        if (p != null) {
            p.setSystemId(uri);
        }
    }

    public boolean equals(NodeInfo other) {
        return this == other;
    }

    public NodeName getNodeName() {
        return null;
    }

    @Override
    public boolean hasFingerprint() {
        return true;
    }

    @Override
    public int getFingerprint() {
        NodeName name = this.getNodeName();
        if (name == null) {
            return -1;
        }
        return name.obtainFingerprint(this.getConfiguration().getNamePool());
    }

    @Override
    public AttributeMap attributes() {
        return EmptyAttributeMap.getInstance();
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        long seq = this.getSequenceNumber();
        if (seq == -1L) {
            this.getPhysicalRoot().generateId(buffer);
            buffer.cat(NODE_LETTER[this.getNodeKind()]);
            buffer.append(Long.toString(seq) + "h" + this.hashCode());
        } else {
            this.parent.generateId(buffer);
            buffer.cat(NODE_LETTER[this.getNodeKind()]);
            buffer.append(Integer.toString(this.index));
        }
    }

    @Override
    public String getSystemId() {
        return this.parent.getSystemId();
    }

    @Override
    public String getBaseURI() {
        return this.parent.getBaseURI();
    }

    protected long getSequenceNumber() {
        NodeImpl prev = this;
        int i = 0;
        while (true) {
            if (prev instanceof ParentNodeImpl) {
                long prevseq = prev.getSequenceNumber();
                return prevseq == -1L ? prevseq : prevseq + 65536L + (long)i;
            }
            assert (prev != null);
            prev = prev.getPreviousInDocument();
            ++i;
        }
    }

    @Override
    public final int compareOrder(NodeInfo other) {
        if (other instanceof NamespaceNode) {
            return 0 - other.compareOrder(this);
        }
        long a = this.getSequenceNumber();
        long b = ((NodeImpl)other).getSequenceNumber();
        if (a == -1L || b == -1L) {
            return Navigator.compareOrder(this, (NodeImpl)other);
        }
        return Long.compare(a, b);
    }

    @Override
    public Configuration getConfiguration() {
        return this.getPhysicalRoot().getConfiguration();
    }

    public NamePool getNamePool() {
        return this.getPhysicalRoot().getNamePool();
    }

    @Override
    public String getPrefix() {
        NodeName qName = this.getNodeName();
        return qName == null ? "" : qName.getPrefix();
    }

    @Override
    public String getURI() {
        NodeName qName = this.getNodeName();
        return qName == null ? "" : qName.getURI();
    }

    @Override
    public String getDisplayName() {
        NodeName qName = this.getNodeName();
        return qName == null ? "" : qName.getDisplayName();
    }

    @Override
    public String getLocalPart() {
        NodeName qName = this.getNodeName();
        return qName == null ? "" : qName.getLocalPart();
    }

    @Override
    public int getLineNumber() {
        return this.parent.getLineNumber();
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public final NodeImpl getParent() {
        if (this.parent instanceof DocumentImpl && ((DocumentImpl)this.parent).isImaginary()) {
            return null;
        }
        return this.parent;
    }

    protected final ParentNodeImpl getRawParent() {
        return this.parent;
    }

    protected final void setRawParent(ParentNodeImpl parent) {
        this.parent = parent;
    }

    @Override
    public NodeImpl getPreviousSibling() {
        if (this.parent == null) {
            return null;
        }
        return this.parent.getNthChild(this.index - 1);
    }

    @Override
    public NodeImpl getNextSibling() {
        if (this.parent == null) {
            return null;
        }
        return this.parent.getNthChild(this.index + 1);
    }

    @Override
    public NodeImpl getFirstChild() {
        return null;
    }

    public NodeInfo getLastChild() {
        return null;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        if (axisNumber == 3) {
            if (this instanceof ParentNodeImpl) {
                return ((ParentNodeImpl)this).iterateChildren(null);
            }
            return EmptyIterator.ofNodes();
        }
        return this.iterateAxis(axisNumber, AnyNodeTest.getInstance());
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        switch (axisNumber) {
            case 0: {
                return new AncestorEnumeration(this, nodeTest, false);
            }
            case 1: {
                return new AncestorEnumeration(this, nodeTest, true);
            }
            case 2: {
                if (this.getNodeKind() != 1) {
                    return EmptyIterator.ofNodes();
                }
                return ((ElementImpl)this).iterateAttributes(nodeTest);
            }
            case 3: {
                if (this instanceof ParentNodeImpl) {
                    return ((ParentNodeImpl)this).iterateChildren(nodeTest);
                }
                return EmptyIterator.ofNodes();
            }
            case 4: {
                if (this.getNodeKind() == 9 && nodeTest instanceof NameTest && ((NameTest)nodeTest).getPrimitiveType() == 1) {
                    return ((DocumentImpl)this).getAllElements(((NameTest)nodeTest).getFingerprint());
                }
                if (this.hasChildNodes()) {
                    return new SteppingNavigator.DescendantAxisIterator<NodeImpl>(this, false, nodeTest);
                }
                return EmptyIterator.ofNodes();
            }
            case 5: {
                return new SteppingNavigator.DescendantAxisIterator<NodeImpl>(this, true, nodeTest);
            }
            case 6: {
                return new FollowingEnumeration(this, nodeTest);
            }
            case 7: {
                return new FollowingSiblingEnumeration(this, nodeTest);
            }
            case 8: {
                if (this.getNodeKind() != 1) {
                    return EmptyIterator.ofNodes();
                }
                return NamespaceNode.makeIterator(this, nodeTest);
            }
            case 9: {
                NodeImpl parent = this.getParent();
                if (parent == null) {
                    return EmptyIterator.ofNodes();
                }
                return Navigator.filteredSingleton(parent, nodeTest);
            }
            case 10: {
                return new PrecedingEnumeration(this, nodeTest);
            }
            case 11: {
                return new PrecedingSiblingEnumeration(this, nodeTest);
            }
            case 12: {
                return Navigator.filteredSingleton(this, nodeTest);
            }
            case 13: {
                return new PrecedingOrAncestorEnumeration(this, nodeTest);
            }
        }
        throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }

    @Override
    public String getAttributeValue(String uri, String localName) {
        return null;
    }

    @Override
    public NodeInfo getRoot() {
        NodeImpl parent = this.getParent();
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    public DocumentImpl getPhysicalRoot() {
        ParentNodeImpl up;
        for (up = this.parent; up != null && !(up instanceof DocumentImpl); up = up.getRawParent()) {
        }
        return (DocumentImpl)up;
    }

    public NodeImpl getNextInDocument(NodeImpl anchor) {
        NodeImpl next = this.getFirstChild();
        if (next != null) {
            return next;
        }
        if (this == anchor) {
            return null;
        }
        next = this.getNextSibling();
        if (next != null) {
            return next;
        }
        NodeImpl parent = this;
        do {
            if ((parent = parent.getParent()) == null) {
                return null;
            }
            if (parent != anchor) continue;
            return null;
        } while ((next = parent.getNextSibling()) == null);
        return next;
    }

    @Override
    public NodeImpl getSuccessorElement(NodeImpl anchor, String uri, String local) {
        NodeImpl next;
        for (next = this.getNextInDocument(anchor); next != null && (next.getNodeKind() != 1 || uri != null && !uri.equals(next.getURI()) || local != null && !local.equals(next.getLocalPart())); next = next.getNextInDocument(anchor)) {
        }
        return next;
    }

    public NodeImpl getPreviousInDocument() {
        NodeImpl prev = this.getPreviousSibling();
        if (prev != null) {
            return prev.getLastDescendantOrSelf();
        }
        return this.getParent();
    }

    private NodeImpl getLastDescendantOrSelf() {
        NodeImpl last = (NodeImpl)this.getLastChild();
        if (last == null) {
            return this;
        }
        return last.getLastDescendantOrSelf();
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        return null;
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        return null;
    }

    @Override
    public boolean hasChildNodes() {
        return this.getFirstChild() != null;
    }

    @Override
    public void setTypeAnnotation(SchemaType type) {
    }

    @Override
    public void delete() {
        if (this.parent != null) {
            this.parent.removeChild(this);
            DocumentImpl newRoot = new DocumentImpl();
            newRoot.setConfiguration(this.getConfiguration());
            newRoot.setImaginary(true);
            this.parent = newRoot;
        }
        this.index = -1;
    }

    @Override
    public boolean isDeleted() {
        return this.index == -1 || this.parent != null && this.parent.isDeleted();
    }

    @Override
    public void setAttributes(AttributeMap attributes) {
        throw new UnsupportedOperationException("setAttributes() applies only to element nodes");
    }

    @Override
    public void removeAttribute(NodeInfo attribute) {
    }

    @Override
    public void addAttribute(NodeName name, SimpleType attType, CharSequence value, int properties) {
    }

    @Override
    public void rename(NodeName newNameCode) {
    }

    @Override
    public void addNamespace(NamespaceBinding nscode) {
    }

    @Override
    public void replace(NodeInfo[] replacement, boolean inherit) {
        if (this.isDeleted()) {
            throw new IllegalStateException("Cannot replace a deleted node");
        }
        if (this.getParent() == null) {
            throw new IllegalStateException("Cannot replace a parentless node");
        }
        assert (this.parent != null);
        this.parent.replaceChildrenAt(replacement, this.index, inherit);
        this.parent = null;
        this.index = -1;
    }

    @Override
    public void insertChildren(NodeInfo[] source, boolean atStart, boolean inherit) {
    }

    @Override
    public void insertSiblings(NodeInfo[] source, boolean before, boolean inherit) {
        if (this.parent == null) {
            throw new IllegalStateException("Cannot add siblings if there is no parent");
        }
        this.parent.insertChildrenAt(source, before ? this.index : this.index + 1, inherit);
    }

    @Override
    public void removeTypeAnnotation() {
    }

    @Override
    public Builder newBuilder() {
        return this.getPhysicalRoot().newBuilder();
    }

    @Override
    public boolean effectiveBooleanValue() throws XPathException {
        return true;
    }
}

