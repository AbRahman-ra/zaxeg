/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.PrependAxisIterator;
import net.sf.saxon.tree.tiny.AncestorIterator;
import net.sf.saxon.tree.tiny.AttributeIterator;
import net.sf.saxon.tree.tiny.DescendantIterator;
import net.sf.saxon.tree.tiny.DescendantIteratorSansText;
import net.sf.saxon.tree.tiny.FollowingIterator;
import net.sf.saxon.tree.tiny.NamedChildIterator;
import net.sf.saxon.tree.tiny.PrecedingIterator;
import net.sf.saxon.tree.tiny.PrecedingSiblingIterator;
import net.sf.saxon.tree.tiny.SiblingIterator;
import net.sf.saxon.tree.tiny.TinyAttributeImpl;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import net.sf.saxon.tree.tiny.TinyParentNodeImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;

public abstract class TinyNodeImpl
implements NodeInfo {
    protected TinyTree tree;
    protected int nodeNr;
    protected TinyNodeImpl parent = null;
    public static final char[] NODE_LETTER = new char[]{'x', 'e', 'a', 't', 'x', 'x', 'x', 'p', 'c', 'r', 'x', 'x', 'x', 'n'};

    @Override
    public Genre getGenre() {
        return Genre.NODE;
    }

    @Override
    public TreeInfo getTreeInfo() {
        return this.tree;
    }

    @Override
    public NodeInfo head() {
        return this;
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.getStringValue();
    }

    @Override
    public SchemaType getSchemaType() {
        return null;
    }

    @Override
    public int getColumnNumber() {
        return this.tree.getColumnNumber(this.nodeNr);
    }

    @Override
    public void setSystemId(String uri) {
        this.tree.setSystemId(this.nodeNr, uri);
    }

    protected void setParentNode(TinyNodeImpl parent) {
        this.parent = parent;
    }

    @Override
    public boolean isSameNodeInfo(NodeInfo other) {
        return this == other || other instanceof TinyNodeImpl && this.tree == ((TinyNodeImpl)other).tree && this.nodeNr == ((TinyNodeImpl)other).nodeNr && this.getNodeKind() == other.getNodeKind();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NodeInfo && this.isSameNodeInfo((NodeInfo)other);
    }

    @Override
    public int hashCode() {
        return (int)(this.tree.getDocumentNumber() & 0x3FFL) << 20 ^ this.nodeNr ^ this.getNodeKind() << 14;
    }

    @Override
    public String getSystemId() {
        return this.tree.getSystemId(this.nodeNr);
    }

    @Override
    public String getBaseURI() {
        return this.getParent().getBaseURI();
    }

    @Override
    public int getLineNumber() {
        return this.tree.getLineNumber(this.nodeNr);
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    protected long getSequenceNumber() {
        return (long)this.nodeNr << 32;
    }

    @Override
    public final int compareOrder(NodeInfo other) {
        long a = this.getSequenceNumber();
        if (other instanceof TinyNodeImpl) {
            long b = ((TinyNodeImpl)other).getSequenceNumber();
            return Long.compare(a, b);
        }
        return 0 - other.compareOrder(this);
    }

    @Override
    public final boolean hasFingerprint() {
        return true;
    }

    @Override
    public int getFingerprint() {
        int nc = this.tree.nameCode[this.nodeNr];
        if (nc == -1) {
            return -1;
        }
        return nc & 0xFFFFF;
    }

    @Override
    public String getPrefix() {
        int code = this.tree.nameCode[this.nodeNr];
        if (code < 0) {
            return "";
        }
        if (!NamePool.isPrefixed(code)) {
            return "";
        }
        return this.tree.prefixPool.getPrefix(code >> 20);
    }

    @Override
    public String getURI() {
        int code = this.tree.nameCode[this.nodeNr];
        if (code < 0) {
            return "";
        }
        return this.tree.getNamePool().getURI(code & 0xFFFFF);
    }

    @Override
    public String getDisplayName() {
        int code = this.tree.nameCode[this.nodeNr];
        if (code < 0) {
            return "";
        }
        if (NamePool.isPrefixed(code)) {
            return this.getPrefix() + ":" + this.getLocalPart();
        }
        return this.getLocalPart();
    }

    @Override
    public String getLocalPart() {
        int code = this.tree.nameCode[this.nodeNr];
        if (code < 0) {
            return "";
        }
        return this.tree.getNamePool().getLocalName(code);
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        if (axisNumber == 3) {
            if (this.hasChildNodes()) {
                return new SiblingIterator(this.tree, this, null, true);
            }
            return EmptyIterator.ofNodes();
        }
        return this.iterateAxis(axisNumber, AnyNodeTest.getInstance());
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> predicate) {
        if (predicate instanceof NodeTest) {
            NodeTest nodeTest = (NodeTest)predicate;
            int type = this.getNodeKind();
            switch (axisNumber) {
                case 0: {
                    return new AncestorIterator(this, nodeTest);
                }
                case 1: {
                    AncestorIterator ancestors = new AncestorIterator(this, nodeTest);
                    if (nodeTest.test(this)) {
                        return new PrependAxisIterator(this, ancestors);
                    }
                    return ancestors;
                }
                case 2: {
                    if (type != 1) {
                        return EmptyIterator.ofNodes();
                    }
                    if (this.tree.alpha[this.nodeNr] < 0) {
                        return EmptyIterator.ofNodes();
                    }
                    return new AttributeIterator(this.tree, this.nodeNr, nodeTest);
                }
                case 3: {
                    if (this.hasChildNodes()) {
                        if (nodeTest instanceof NameTest && ((NameTest)nodeTest).getNodeKind() == 1) {
                            return new NamedChildIterator(this.tree, this, nodeTest.getFingerprint());
                        }
                        return new SiblingIterator(this.tree, this, nodeTest, true);
                    }
                    return EmptyIterator.ofNodes();
                }
                case 4: {
                    if (type == 9 && nodeTest instanceof NameTest && nodeTest.getPrimitiveType() == 1) {
                        return ((TinyDocumentImpl)this).getAllElements(nodeTest.getFingerprint());
                    }
                    if (this.hasChildNodes()) {
                        if (nodeTest.getUType().overlaps(UType.TEXT)) {
                            return new DescendantIterator(this.tree, this, nodeTest);
                        }
                        return new DescendantIteratorSansText(this.tree, this, nodeTest);
                    }
                    return EmptyIterator.ofNodes();
                }
                case 5: {
                    AxisIterator descendants = this.iterateAxis(4, nodeTest);
                    if (nodeTest.test(this)) {
                        return new PrependAxisIterator(this, descendants);
                    }
                    return descendants;
                }
                case 6: {
                    if (type == 2 || type == 13) {
                        return new FollowingIterator(this.tree, this.getParent(), nodeTest, true);
                    }
                    if (this.tree.depth[this.nodeNr] == 0) {
                        return EmptyIterator.ofNodes();
                    }
                    return new FollowingIterator(this.tree, this, nodeTest, false);
                }
                case 7: {
                    if (type == 2 || type == 13 || this.tree.depth[this.nodeNr] == 0) {
                        return EmptyIterator.ofNodes();
                    }
                    return new SiblingIterator(this.tree, this, nodeTest, false);
                }
                case 8: {
                    if (type != 1) {
                        return EmptyIterator.ofNodes();
                    }
                    return NamespaceNode.makeIterator(this, nodeTest);
                }
                case 9: {
                    TinyNodeImpl parent = this.getParent();
                    return Navigator.filteredSingleton(parent, nodeTest);
                }
                case 10: {
                    if (type == 2 || type == 13) {
                        return this.getParent().iterateAxis(axisNumber, predicate);
                    }
                    if (this.tree.depth[this.nodeNr] == 0) {
                        return EmptyIterator.ofNodes();
                    }
                    return new PrecedingIterator(this.tree, this, nodeTest, false);
                }
                case 11: {
                    if (type == 2 || type == 13 || this.tree.depth[this.nodeNr] == 0) {
                        return EmptyIterator.ofNodes();
                    }
                    return new PrecedingSiblingIterator(this.tree, this, nodeTest);
                }
                case 12: {
                    return Navigator.filteredSingleton(this, nodeTest);
                }
                case 13: {
                    if (type == 9) {
                        return EmptyIterator.ofNodes();
                    }
                    if (type == 2 || type == 13) {
                        TinyNodeImpl el = this.getParent();
                        return new PrependAxisIterator(el, new PrecedingIterator(this.tree, el, nodeTest, true));
                    }
                    return new PrecedingIterator(this.tree, this, nodeTest, true);
                }
            }
            throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }
        return new Navigator.AxisFilter(this.iterateAxis(axisNumber, AnyNodeTest.getInstance()), predicate);
    }

    @Override
    public TinyNodeImpl getParent() {
        if (this.parent != null) {
            return this.parent;
        }
        int p = TinyNodeImpl.getParentNodeNr(this.tree, this.nodeNr);
        if (p == -1) {
            return null;
        }
        this.parent = this.tree.getNode(p);
        return this.parent;
    }

    static int getParentNodeNr(TinyTree tree, int nodeNr) {
        if (tree.depth[nodeNr] == 0) {
            return -1;
        }
        int p = tree.next[nodeNr];
        while (p > nodeNr) {
            if (tree.nodeKind[p] == 12) {
                return tree.alpha[p];
            }
            p = tree.next[p];
        }
        return p;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return null;
    }

    @Override
    public NodeInfo getRoot() {
        return this.nodeNr == 0 ? this : this.tree.getRootNode();
    }

    @Override
    public Configuration getConfiguration() {
        return this.tree.getConfiguration();
    }

    public NamePool getNamePool() {
        return this.tree.getNamePool();
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
    public void generateId(FastStringBuffer buffer) {
        buffer.append("d");
        buffer.append(Long.toString(this.tree.getDocumentNumber()));
        buffer.cat(NODE_LETTER[this.getNodeKind()]);
        buffer.append(Integer.toString(this.nodeNr));
    }

    public boolean isAncestorOrSelf(TinyNodeImpl d) {
        if (this.tree != d.tree) {
            return false;
        }
        int dn = d.nodeNr;
        if (d instanceof TinyAttributeImpl) {
            if (this instanceof TinyAttributeImpl) {
                return this.nodeNr == dn;
            }
            dn = this.tree.attParent[dn];
        }
        if (this instanceof TinyAttributeImpl) {
            return false;
        }
        if (this.nodeNr > dn) {
            return false;
        }
        if (this.nodeNr == dn) {
            return true;
        }
        if (!(this instanceof TinyParentNodeImpl)) {
            return false;
        }
        if (this.tree.depth[this.nodeNr] >= this.tree.depth[dn]) {
            return false;
        }
        int n = this.nodeNr;
        while (true) {
            int nextSib;
            if ((nextSib = this.tree.next[n]) < 0 || nextSib > dn) {
                return true;
            }
            if (this.tree.depth[nextSib] == 0) {
                return true;
            }
            if (nextSib >= n) break;
            n = nextSib;
        }
        return false;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean isIdref() {
        return false;
    }

    @Override
    public boolean isNilled() {
        return this.tree.isNilled(this.nodeNr);
    }

    @Override
    public boolean isStreamed() {
        return false;
    }

    public TinyTree getTree() {
        return this.tree;
    }

    public int getNodeNumber() {
        return this.nodeNr;
    }
}

