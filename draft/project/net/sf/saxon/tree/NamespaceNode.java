/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.ListIterator;
import net.sf.saxon.tree.iter.PrependAxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.StringValue;

public class NamespaceNode
implements NodeInfo {
    NodeInfo element;
    NamespaceBinding nsBinding;
    int position;
    int fingerprint;

    public NamespaceNode(NodeInfo element, NamespaceBinding nscode, int position) {
        this.element = element;
        this.nsBinding = nscode;
        this.position = position;
        this.fingerprint = -1;
    }

    @Override
    public TreeInfo getTreeInfo() {
        return this.element.getTreeInfo();
    }

    @Override
    public NodeInfo head() {
        return this;
    }

    @Override
    public int getNodeKind() {
        return 13;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NamespaceNode && this.element.equals(((NamespaceNode)other).element) && this.nsBinding.equals(((NamespaceNode)other).nsBinding);
    }

    @Override
    public int hashCode() {
        return this.element.hashCode() ^ this.position << 13;
    }

    @Override
    public String getSystemId() {
        return this.element.getSystemId();
    }

    @Override
    public String getPublicId() {
        return this.element.getPublicId();
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public int getLineNumber() {
        return this.element.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return this.element.getColumnNumber();
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof NamespaceNode && this.element.equals(((NamespaceNode)other).element)) {
            int c = this.position - ((NamespaceNode)other).position;
            return Integer.compare(c, 0);
        }
        if (this.element.equals(other)) {
            return 1;
        }
        return this.element.compareOrder(other);
    }

    @Override
    public String getStringValue() {
        return this.nsBinding.getURI();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.getStringValue();
    }

    @Override
    public boolean hasFingerprint() {
        return true;
    }

    @Override
    public int getFingerprint() {
        if (this.fingerprint == -1) {
            if (this.nsBinding.getPrefix().isEmpty()) {
                return -1;
            }
            this.fingerprint = this.element.getConfiguration().getNamePool().allocateFingerprint("", this.nsBinding.getPrefix());
        }
        return this.fingerprint;
    }

    @Override
    public String getLocalPart() {
        return this.nsBinding.getPrefix();
    }

    @Override
    public String getURI() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return this.getLocalPart();
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public Configuration getConfiguration() {
        return this.element.getConfiguration();
    }

    public NamePool getNamePool() {
        return this.getConfiguration().getNamePool();
    }

    @Override
    public SchemaType getSchemaType() {
        return BuiltInAtomicType.STRING;
    }

    @Override
    public NodeInfo getParent() {
        return this.element;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        switch (axisNumber) {
            case 0: {
                return this.element.iterateAxis(1, nodeTest);
            }
            case 1: {
                if (nodeTest.test(this)) {
                    return new PrependAxisIterator(this, this.element.iterateAxis(1, nodeTest));
                }
                return this.element.iterateAxis(1, nodeTest);
            }
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 7: 
            case 8: 
            case 11: {
                return EmptyIterator.ofNodes();
            }
            case 6: {
                return new Navigator.AxisFilter(new Navigator.FollowingEnumeration(this), nodeTest);
            }
            case 9: {
                return Navigator.filteredSingleton(this.element, nodeTest);
            }
            case 10: {
                return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, false), nodeTest);
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

    @Override
    public String getAttributeValue(String uri, String local) {
        return null;
    }

    @Override
    public NodeInfo getRoot() {
        return this.element.getRoot();
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        this.element.generateId(buffer);
        buffer.append("n");
        buffer.append(Integer.toString(this.position));
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.append(this);
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
    public void setSystemId(String systemId) {
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return new StringValue(this.getStringValueCS());
    }

    @Override
    public boolean isStreamed() {
        return this.element.isStreamed();
    }

    public static AxisIterator makeIterator(NodeInfo element, Predicate<? super NodeInfo> test) {
        NamespaceNode node;
        ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();
        Iterator<NamespaceBinding> bindings = element.getAllNamespaces().iterator();
        int position = 0;
        boolean foundXML = false;
        while (bindings.hasNext()) {
            NamespaceNode node2;
            NamespaceBinding binding = bindings.next();
            if (binding.getPrefix().equals("xml")) {
                foundXML = true;
            }
            if (!test.test(node2 = new NamespaceNode(element, binding, position++))) continue;
            nodes.add(node2);
        }
        if (!foundXML && test.test(node = new NamespaceNode(element, NamespaceBinding.XML, position))) {
            nodes.add(node);
        }
        return new ListIterator.OfNodes((List<NodeInfo>)nodes);
    }
}

