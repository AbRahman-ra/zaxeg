/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.tiny;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import javax.xml.transform.SourceLocator;
import net.sf.saxon.event.CopyInformee;
import net.sf.saxon.event.CopyNamespaceSensitiveException;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.CopyOptions;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.NameOfNode;
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
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.z.IntValuePredicate;

public class TinyTextualElement
extends TinyElementImpl {
    private TinyTextualElementText textNode = null;

    public TinyTextualElement(TinyTree tree, int nodeNr) {
        super(tree, nodeNr);
    }

    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        return NamespaceBinding.EMPTY_ARRAY;
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        TinyNodeImpl parent = this.getParent();
        if (parent instanceof TinyElementImpl) {
            return parent.getAllNamespaces();
        }
        return NamespaceMap.emptyMap();
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return null;
    }

    @Override
    public String getAttributeValue(int fp) {
        return null;
    }

    @Override
    public void copy(Receiver receiver, int copyOptions, Location location) throws XPathException {
        Object o;
        CopyInformee informee;
        boolean disallowNamespaceSensitiveContent;
        boolean typed = CopyOptions.includes(copyOptions, 4);
        SchemaType type = typed ? this.getSchemaType() : Untyped.getInstance();
        boolean bl = disallowNamespaceSensitiveContent = (copyOptions & 4) != 0 && (copyOptions & 2) == 0;
        if (disallowNamespaceSensitiveContent) {
            try {
                this.checkNotNamespaceSensitiveElement(type, this.nodeNr);
            } catch (CopyNamespaceSensitiveException e) {
                e.setErrorCode(receiver.getPipelineConfiguration().isXSLT() ? "XTTE0950" : "XQTY0086");
                throw e;
            }
        }
        if ((informee = (CopyInformee)receiver.getPipelineConfiguration().getComponent(CopyInformee.class.getName())) != null && (o = informee.notifyElementNode(this)) instanceof Location) {
            location = (Location)o;
        }
        NamespaceMap namespaces = (copyOptions & 2) != 0 ? this.getAllNamespaces() : NamespaceMap.emptyMap();
        receiver.startElement(NameOfNode.makeName(this), type, EmptyAttributeMap.getInstance(), namespaces, location, 0);
        receiver.characters(this.getStringValueCS(), location, 0);
        receiver.endElement();
    }

    @Override
    public boolean hasChildNodes() {
        return true;
    }

    @Override
    public CharSequence getStringValueCS() {
        return TinyTextImpl.getStringValue(this.tree, this.nodeNr);
    }

    @Override
    public String getStringValue() {
        return TinyTextImpl.getStringValue(this.tree, this.nodeNr).toString();
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        switch (axisNumber) {
            case 2: {
                return EmptyIterator.ofNodes();
            }
            case 3: 
            case 4: {
                return SingleNodeIterator.makeIterator(this.getTextNode());
            }
            case 5: {
                ArrayList<NodeInfo> list = new ArrayList<NodeInfo>(2);
                list.add(this);
                list.add(this.getTextNode());
                return new ListIterator.OfNodes((List<NodeInfo>)list);
            }
        }
        return super.iterateAxis(axisNumber);
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        switch (axisNumber) {
            case 2: {
                return EmptyIterator.ofNodes();
            }
            case 3: 
            case 4: {
                return Navigator.filteredSingleton(this.getTextNode(), nodeTest);
            }
            case 5: {
                ArrayList<NodeInfo> list = new ArrayList<NodeInfo>(2);
                if (nodeTest.test(this)) {
                    list.add(this);
                }
                if (nodeTest.test(this.getTextNode())) {
                    list.add(this.getTextNode());
                }
                return new ListIterator.OfNodes((List<NodeInfo>)list);
            }
        }
        return super.iterateAxis(axisNumber, nodeTest);
    }

    @Override
    public boolean isAncestorOrSelf(TinyNodeImpl d) {
        return this.equals(d);
    }

    public TinyTextualElementText getTextNode() {
        if (this.textNode == null) {
            this.textNode = new TinyTextualElementText();
        }
        return this.textNode;
    }

    public class TinyTextualElementText
    implements NodeInfo,
    SourceLocator {
        private IntPredicate isNewline = new IntValuePredicate(10);

        @Override
        public boolean hasFingerprint() {
            return true;
        }

        @Override
        public TreeInfo getTreeInfo() {
            return TinyTextualElement.this.getTreeInfo();
        }

        @Override
        public void setSystemId(String systemId) {
        }

        @Override
        public final int getNodeKind() {
            return 3;
        }

        @Override
        public String getStringValue() {
            return this.getStringValueCS().toString();
        }

        @Override
        public CharSequence getStringValueCS() {
            return TinyTextualElement.this.getStringValueCS();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof TinyTextualElementText && this.getParent().equals(((TinyTextualElementText)other).getParent());
        }

        @Override
        public void generateId(FastStringBuffer buffer) {
            TinyTextualElement.this.generateId(buffer);
            buffer.append("T");
        }

        @Override
        public String getSystemId() {
            return TinyTextualElement.this.getSystemId();
        }

        @Override
        public String getBaseURI() {
            return TinyTextualElement.this.getBaseURI();
        }

        @Override
        public int compareOrder(NodeInfo other) {
            if (other.equals(this)) {
                return 0;
            }
            if (other.equals(this.getParent())) {
                return 1;
            }
            return this.getParent().compareOrder(other);
        }

        @Override
        public int getFingerprint() {
            return -1;
        }

        @Override
        public String getPrefix() {
            return "";
        }

        @Override
        public String getURI() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public String getLocalPart() {
            return "";
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
        public int getLineNumber() {
            return this.getParent().getLineNumber();
        }

        @Override
        public int getColumnNumber() {
            return this.getParent().getColumnNumber();
        }

        @Override
        public Location saveLocation() {
            return this;
        }

        @Override
        public SchemaType getSchemaType() {
            return null;
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
        public AtomicSequence atomize() throws XPathException {
            return new UntypedAtomicValue(this.getStringValueCS());
        }

        @Override
        public AxisIterator iterateAxis(int axisNumber) {
            switch (axisNumber) {
                case 0: {
                    return TinyTextualElement.this.iterateAxis(1);
                }
                case 13: {
                    return new Navigator.PrecedingEnumeration(this, true);
                }
                case 1: {
                    return new PrependAxisIterator(this, this.getParent().iterateAxis(1));
                }
                case 6: {
                    return new Navigator.FollowingEnumeration(this);
                }
                case 10: {
                    return new Navigator.PrecedingEnumeration(this, false);
                }
                case 9: {
                    return SingleNodeIterator.makeIterator(this.getParent());
                }
                case 2: 
                case 3: 
                case 4: 
                case 7: 
                case 8: 
                case 11: {
                    return EmptyIterator.ofNodes();
                }
                case 5: 
                case 12: {
                    return SingleNodeIterator.makeIterator(this);
                }
            }
            throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }

        @Override
        public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
            switch (axisNumber) {
                case 0: {
                    return this.getParent().iterateAxis(1, nodeTest);
                }
                case 13: {
                    return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, true), nodeTest);
                }
                case 1: {
                    return new Navigator.AxisFilter(new PrependAxisIterator(this, this.getParent().iterateAxis(1)), nodeTest);
                }
                case 6: {
                    return new Navigator.AxisFilter(new Navigator.FollowingEnumeration(this), nodeTest);
                }
                case 10: {
                    return new Navigator.AxisFilter(new Navigator.PrecedingEnumeration(this, false), nodeTest);
                }
                case 9: {
                    return Navigator.filteredSingleton(this.getParent(), nodeTest);
                }
                case 2: 
                case 3: 
                case 4: 
                case 7: 
                case 8: 
                case 11: {
                    return EmptyIterator.ofNodes();
                }
                case 5: 
                case 12: {
                    return Navigator.filteredSingleton(this, nodeTest);
                }
            }
            throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }

        @Override
        public NodeInfo getParent() {
            return TinyTextualElement.this;
        }

        @Override
        public NodeInfo getRoot() {
            return this.getParent().getRoot();
        }

        @Override
        public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
            out.characters(this.getStringValueCS(), locationId, 0);
        }
    }
}

