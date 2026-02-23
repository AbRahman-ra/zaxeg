/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.function.Predicate;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.UntypedAtomicValue;

public abstract class AbstractNodeWrapper
implements NodeInfo,
VirtualNode {
    protected TreeInfo treeInfo;

    @Override
    public TreeInfo getTreeInfo() {
        return this.treeInfo;
    }

    @Override
    public final Object getRealNode() {
        return this.getUnderlyingNode();
    }

    public NamePool getNamePool() {
        return this.getConfiguration().getNamePool();
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        switch (this.getNodeKind()) {
            case 7: 
            case 8: {
                return new StringValue(this.getStringValueCS());
            }
        }
        return new UntypedAtomicValue(this.getStringValueCS());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AbstractNodeWrapper)) {
            return false;
        }
        AbstractNodeWrapper ow = (AbstractNodeWrapper)other;
        return this.getUnderlyingNode().equals(ow.getUnderlyingNode());
    }

    @Override
    public int hashCode() {
        return this.getUnderlyingNode().hashCode();
    }

    @Override
    public String getSystemId() {
        if (this.treeInfo instanceof GenericTreeInfo) {
            return ((GenericTreeInfo)this.treeInfo).getSystemId();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSystemId(String uri) {
        if (!(this.treeInfo instanceof GenericTreeInfo)) {
            throw new UnsupportedOperationException();
        }
        ((GenericTreeInfo)this.treeInfo).setSystemId(uri);
    }

    @Override
    public String getBaseURI() {
        if (this.getNodeKind() == 13) {
            return null;
        }
        NodeInfo n = this;
        if (this.getNodeKind() != 1) {
            n = this.getParent();
        }
        while (n != null) {
            String xmlbase = n.getAttributeValue("http://www.w3.org/XML/1998/namespace", "base");
            if (xmlbase != null) {
                return xmlbase;
            }
            n = n.getParent();
        }
        return this.getRoot().getSystemId();
    }

    @Override
    public int getLineNumber() {
        return -1;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public String getDisplayName() {
        String prefix = this.getPrefix();
        String local = this.getLocalPart();
        if (prefix.isEmpty()) {
            return local;
        }
        return prefix + ":" + local;
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return null;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        return this.iterateAxis(axisNumber, AnyNodeTest.getInstance());
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        int nodeKind = this.getNodeKind();
        switch (axisNumber) {
            case 0: {
                if (nodeKind == 9) {
                    return EmptyIterator.ofNodes();
                }
                return new Navigator.AxisFilter(new Navigator.AncestorEnumeration(this, false), nodeTest);
            }
            case 1: {
                if (nodeKind == 9) {
                    return Navigator.filteredSingleton(this, nodeTest);
                }
                return new Navigator.AxisFilter(new Navigator.AncestorEnumeration(this, true), nodeTest);
            }
            case 2: {
                if (nodeKind != 1) {
                    return EmptyIterator.ofNodes();
                }
                return this.iterateAttributes(nodeTest);
            }
            case 3: {
                if (nodeKind == 1 || nodeKind == 9) {
                    return this.iterateChildren(nodeTest);
                }
                return EmptyIterator.ofNodes();
            }
            case 4: {
                if (nodeKind == 1 || nodeKind == 9) {
                    return this.iterateDescendants(nodeTest, false);
                }
                return EmptyIterator.ofNodes();
            }
            case 5: {
                if (nodeKind == 1 || nodeKind == 9) {
                    return this.iterateDescendants(nodeTest, true);
                }
                return Navigator.filteredSingleton(this, nodeTest);
            }
            case 6: {
                return new Navigator.AxisFilter(new Navigator.FollowingEnumeration(this), nodeTest);
            }
            case 7: {
                switch (nodeKind) {
                    case 2: 
                    case 9: 
                    case 13: {
                        return EmptyIterator.ofNodes();
                    }
                }
                return this.iterateSiblings(nodeTest, true);
            }
            case 8: {
                if (nodeKind != 1) {
                    return EmptyIterator.ofNodes();
                }
                return NamespaceNode.makeIterator(this, nodeTest);
            }
            case 9: {
                return Navigator.filteredSingleton(this.getParent(), nodeTest);
            }
            case 10: {
                return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, false), nodeTest);
            }
            case 11: {
                switch (nodeKind) {
                    case 2: 
                    case 9: 
                    case 13: {
                        return EmptyIterator.ofNodes();
                    }
                }
                return this.iterateSiblings(nodeTest, false);
            }
            case 12: {
                return Navigator.filteredSingleton(this, nodeTest);
            }
            case 13: {
                return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, true), nodeTest);
            }
        }
        throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }

    protected abstract AxisIterator iterateAttributes(Predicate<? super NodeInfo> var1);

    protected abstract AxisIterator iterateChildren(Predicate<? super NodeInfo> var1);

    protected abstract AxisIterator iterateSiblings(Predicate<? super NodeInfo> var1, boolean var2);

    protected AxisIterator iterateDescendants(Predicate<? super NodeInfo> nodeTest, boolean includeSelf) {
        AxisIterator iter = new Navigator.DescendantEnumeration(this, includeSelf, true);
        if (!(nodeTest instanceof AnyNodeTest)) {
            iter = new Navigator.AxisFilter(iter, nodeTest);
        }
        return iter;
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        return new NamespaceBinding[0];
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        if (this.getNodeKind() == 1) {
            throw new AssertionError((Object)("not implemented for " + this.getClass()));
        }
        return null;
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
        switch (this.getNodeKind()) {
            case 1: 
            case 9: {
                return this.iterateAxis(3).next() != null;
            }
        }
        return false;
    }

    @Override
    public int getFingerprint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasFingerprint() {
        return false;
    }
}

