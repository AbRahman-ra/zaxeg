/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.wrapper;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.NamespaceNode;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.wrapper.VirtualTreeInfo;
import net.sf.saxon.type.SchemaType;

public class VirtualCopy
implements NodeInfo {
    protected Supplier<String> systemIdSupplier;
    protected NodeInfo original;
    protected VirtualCopy parent;
    protected NodeInfo root;
    protected VirtualTreeInfo tree;
    private boolean dropNamespaces = false;

    protected VirtualCopy(NodeInfo base, NodeInfo root) {
        this.original = base;
        this.systemIdSupplier = base::getBaseURI;
        this.root = root;
    }

    public static VirtualCopy makeVirtualCopy(NodeInfo original) {
        while (original instanceof VirtualCopy) {
            original = ((VirtualCopy)original).original;
        }
        VirtualCopy vc = new VirtualCopy(original, original);
        Configuration config = original.getConfiguration();
        VirtualTreeInfo doc = new VirtualTreeInfo(config, vc);
        long docNr = config.getDocumentNumberAllocator().allocateDocumentNumber();
        doc.setDocumentNumber(docNr);
        vc.tree = doc;
        return vc;
    }

    protected VirtualCopy wrap(NodeInfo node) {
        VirtualCopy vc = new VirtualCopy(node, this.root);
        vc.tree = this.tree;
        vc.systemIdSupplier = this.systemIdSupplier;
        vc.dropNamespaces = this.dropNamespaces;
        return vc;
    }

    public NodeInfo getOriginalNode() {
        return this.original;
    }

    @Override
    public VirtualTreeInfo getTreeInfo() {
        return this.tree;
    }

    public void setDropNamespaces(boolean drop) {
        this.dropNamespaces = drop;
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        if (this.getNodeKind() == 1) {
            if (this.dropNamespaces) {
                NodeInfo att;
                NamespaceMap nsMap = NamespaceMap.emptyMap();
                String ns = this.getURI();
                if (!ns.isEmpty()) {
                    nsMap = nsMap.put(this.getPrefix(), ns);
                }
                AxisIterator iter = this.original.iterateAxis(2);
                while ((att = iter.next()) != null) {
                    if (att.getURI().equals("")) continue;
                    nsMap = nsMap.put(att.getPrefix(), att.getURI());
                }
                return nsMap;
            }
            return this.original.getAllNamespaces();
        }
        return null;
    }

    @Override
    public int getFingerprint() {
        return this.original.getFingerprint();
    }

    @Override
    public boolean hasFingerprint() {
        return this.original.hasFingerprint();
    }

    @Override
    public int getNodeKind() {
        return this.original.getNodeKind();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VirtualCopy && this.getTreeInfo() == ((VirtualCopy)other).getTreeInfo() && this.original.equals(((VirtualCopy)other).original);
    }

    @Override
    public int hashCode() {
        return this.original.hashCode() ^ (int)(this.getTreeInfo().getDocumentNumber() & Integer.MAX_VALUE) << 19;
    }

    @Override
    public String getSystemId() {
        return this.systemIdSupplier.get();
    }

    @Override
    public String getBaseURI() {
        return Navigator.getBaseURI(this);
    }

    @Override
    public int getLineNumber() {
        return this.original.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return this.original.getColumnNumber();
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof VirtualCopy) {
            int c = this.root.compareOrder(((VirtualCopy)other).root);
            if (c == 0) {
                return this.original.compareOrder(((VirtualCopy)other).original);
            }
            return c;
        }
        return other.compareOrder(this.original);
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.original.getStringValueCS();
    }

    @Override
    public String getLocalPart() {
        return this.original.getLocalPart();
    }

    @Override
    public String getURI() {
        return this.original.getURI();
    }

    @Override
    public String getPrefix() {
        return this.original.getPrefix();
    }

    @Override
    public String getDisplayName() {
        return this.original.getDisplayName();
    }

    @Override
    public Configuration getConfiguration() {
        return this.original.getConfiguration();
    }

    @Override
    public SchemaType getSchemaType() {
        return this.original.getSchemaType();
    }

    @Override
    public NodeInfo getParent() {
        if (this.original.equals(this.root)) {
            return null;
        }
        if (this.parent == null) {
            NodeInfo basep = this.original.getParent();
            if (basep == null) {
                return null;
            }
            this.parent = this.wrap(basep);
        }
        return this.parent;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        VirtualCopy newParent = null;
        switch (axisNumber) {
            case 2: 
            case 3: {
                newParent = this;
                break;
            }
            case 7: 
            case 11: 
            case 12: {
                newParent = this.parent;
                break;
            }
            case 0: {
                return new Navigator.AxisFilter(new Navigator.AncestorEnumeration(this, false), nodeTest);
            }
            case 1: {
                return new Navigator.AxisFilter(new Navigator.AncestorEnumeration(this, true), nodeTest);
            }
            case 8: {
                if (this.getNodeKind() != 1) {
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
            case 6: {
                return new Navigator.AxisFilter(new Navigator.FollowingEnumeration(this), nodeTest);
            }
            case 13: {
                return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, true), nodeTest);
            }
        }
        return this.makeCopier(this.original.iterateAxis(axisNumber, nodeTest), newParent, !AxisInfo.isSubtreeAxis[axisNumber]);
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return this.original.getAttributeValue(uri, local);
    }

    @Override
    public NodeInfo getRoot() {
        NodeInfo n = this;
        NodeInfo p;
        while ((p = n.getParent()) != null) {
            n = p;
        }
        return n;
    }

    @Override
    public boolean hasChildNodes() {
        return this.original.hasChildNodes();
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        buffer.append("d");
        buffer.append(Long.toString(this.getTreeInfo().getDocumentNumber()));
        this.original.generateId(buffer);
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        if (this.dropNamespaces) {
            copyOptions &= 0xFFFFFFFD;
        }
        this.original.copy(out, copyOptions, locationId);
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        if (this.getNodeKind() == 1) {
            if (this.dropNamespaces) {
                ArrayList<NamespaceBinding> allNamespaces = new ArrayList<NamespaceBinding>(5);
                String ns = this.getURI();
                if (ns.isEmpty()) {
                    if (this.getParent() != null && !this.getParent().getURI().isEmpty()) {
                        allNamespaces.add(new NamespaceBinding("", ""));
                    }
                } else {
                    allNamespaces.add(new NamespaceBinding(this.getPrefix(), this.getURI()));
                }
                for (AttributeInfo att : this.original.attributes()) {
                    NamespaceBinding b;
                    NodeName name = att.getNodeName();
                    if (name.getURI() == null || allNamespaces.contains(b = new NamespaceBinding(name.getPrefix(), name.getURI()))) continue;
                    allNamespaces.add(b);
                }
                return allNamespaces.toArray(NamespaceBinding.EMPTY_ARRAY);
            }
            if (this.original == this.root) {
                ArrayList<NamespaceBinding> bindings = new ArrayList<NamespaceBinding>();
                for (NamespaceBinding binding : this.original.getAllNamespaces()) {
                    bindings.add(binding);
                }
                return bindings.toArray(NamespaceBinding.EMPTY_ARRAY);
            }
            return this.original.getDeclaredNamespaces(buffer);
        }
        return null;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemIdSupplier = () -> systemId;
    }

    @Override
    public AtomicSequence atomize() throws XPathException {
        return this.original.atomize();
    }

    @Override
    public boolean isId() {
        return this.original.isId();
    }

    @Override
    public boolean isIdref() {
        return this.original.isIdref();
    }

    @Override
    public boolean isNilled() {
        return this.original.isNilled();
    }

    @Override
    public String getPublicId() {
        return this.original != null ? this.original.getPublicId() : null;
    }

    protected boolean isIncludedInCopy(NodeInfo sourceNode) {
        return Navigator.isAncestorOrSelf(this.root, sourceNode);
    }

    protected VirtualCopier makeCopier(AxisIterator axis, VirtualCopy newParent, boolean testInclusion) {
        return new VirtualCopier(axis, newParent, testInclusion);
    }

    protected class VirtualCopier
    implements AxisIterator {
        protected AxisIterator base;
        private VirtualCopy parent;
        protected boolean testInclusion;

        public VirtualCopier(AxisIterator base, VirtualCopy parent, boolean testInclusion) {
            this.base = base;
            this.parent = parent;
            this.testInclusion = testInclusion;
        }

        @Override
        public NodeInfo next() {
            NodeInfo next = this.base.next();
            if (next != null) {
                if (this.testInclusion && !VirtualCopy.this.isIncludedInCopy(next)) {
                    return null;
                }
                VirtualCopy vc = VirtualCopy.this.wrap(next);
                vc.parent = this.parent;
                vc.systemIdSupplier = VirtualCopy.this.systemIdSupplier;
                next = vc;
            }
            return next;
        }

        @Override
        public void close() {
            this.base.close();
        }
    }
}

