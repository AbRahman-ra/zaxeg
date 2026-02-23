/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.value;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import javax.xml.transform.SourceLocator;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.GenericTreeInfo;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.UntypedAtomicValue;

public final class TextFragmentValue
implements NodeInfo,
SourceLocator {
    private CharSequence text;
    private String baseURI;
    private String documentURI;
    private GenericTreeInfo treeInfo;
    private TextFragmentTextNode textNode = null;

    public TextFragmentValue(Configuration config, CharSequence value, String baseURI) {
        this.text = value;
        this.baseURI = baseURI;
        this.treeInfo = new GenericTreeInfo(config);
        this.treeInfo.setRootNode(this);
    }

    public static NodeInfo makeTextFragment(Configuration config, CharSequence value, String baseURI) {
        if (value.length() == 0) {
            DocumentImpl doc = new DocumentImpl();
            doc.setSystemId(baseURI);
            doc.setBaseURI(baseURI);
            doc.setConfiguration(config);
            return doc;
        }
        return new TextFragmentValue(config, value, baseURI);
    }

    @Override
    public TreeInfo getTreeInfo() {
        return this.treeInfo;
    }

    public NodeInfo getRootNode() {
        return this;
    }

    public boolean isTyped() {
        return false;
    }

    @Override
    public final int getNodeKind() {
        return 9;
    }

    @Override
    public String getStringValue() {
        return this.text.toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        return this.text;
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public boolean hasFingerprint() {
        return true;
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        buffer.append("tt");
        buffer.append(Long.toString(this.treeInfo.getDocumentNumber()));
    }

    @Override
    public void setSystemId(String systemId) {
        this.documentURI = systemId;
    }

    @Override
    public String getSystemId() {
        return this.documentURI;
    }

    @Override
    public String getBaseURI() {
        return this.baseURI;
    }

    @Override
    public int compareOrder(NodeInfo other) {
        if (this == other) {
            return 0;
        }
        return -1;
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
        return this.text.length() != 0;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    @Override
    public SchemaType getSchemaType() {
        return Untyped.getInstance();
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
    public AtomicSequence atomize() {
        return new UntypedAtomicValue(this.text);
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        return null;
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber) {
        switch (axisNumber) {
            case 0: 
            case 2: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 13: {
                return EmptyIterator.ofNodes();
            }
            case 1: 
            case 12: {
                return SingleNodeIterator.makeIterator(this);
            }
            case 3: 
            case 4: {
                return SingleNodeIterator.makeIterator(this.getTextNode());
            }
            case 5: {
                NodeInfo[] nodes = new NodeInfo[]{this, this.getTextNode()};
                return new ArrayIterator.OfNodes(nodes);
            }
        }
        throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }

    @Override
    public AxisIterator iterateAxis(int axisNumber, Predicate<? super NodeInfo> nodeTest) {
        switch (axisNumber) {
            case 0: 
            case 2: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 13: {
                return EmptyIterator.ofNodes();
            }
            case 1: 
            case 12: {
                return Navigator.filteredSingleton(this, nodeTest);
            }
            case 3: 
            case 4: {
                return Navigator.filteredSingleton(this.getTextNode(), nodeTest);
            }
            case 5: {
                boolean b1 = nodeTest.test(this);
                TextFragmentTextNode textNode2 = this.getTextNode();
                boolean b2 = nodeTest.test(textNode2);
                if (b1) {
                    if (b2) {
                        NodeInfo[] pair = new NodeInfo[]{this, textNode2};
                        return new ArrayIterator.OfNodes(pair);
                    }
                    return SingleNodeIterator.makeIterator(this);
                }
                if (b2) {
                    return SingleNodeIterator.makeIterator(textNode2);
                }
                return EmptyIterator.ofNodes();
            }
        }
        throw new IllegalArgumentException("Unknown axis number " + axisNumber);
    }

    @Override
    public NodeInfo getParent() {
        return null;
    }

    @Override
    public NodeInfo getRoot() {
        return this;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.characters(this.text, locationId, 0);
    }

    public NodeInfo selectID(String id, boolean getParent) {
        return null;
    }

    public Iterator<String> getUnparsedEntityNames() {
        return Collections.emptyIterator();
    }

    public String[] getUnparsedEntity(String name) {
        return null;
    }

    private TextFragmentTextNode getTextNode() {
        if (this.textNode == null) {
            this.textNode = new TextFragmentTextNode();
        }
        return this.textNode;
    }

    private class TextFragmentTextNode
    implements NodeInfo,
    SourceLocator {
        private TextFragmentTextNode() {
        }

        @Override
        public boolean hasFingerprint() {
            return true;
        }

        @Override
        public TreeInfo getTreeInfo() {
            return TextFragmentValue.this.treeInfo;
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
            return TextFragmentValue.this.text.toString();
        }

        @Override
        public CharSequence getStringValueCS() {
            return TextFragmentValue.this.text;
        }

        public boolean equals(NodeInfo other) {
            return this == other;
        }

        @Override
        public void generateId(FastStringBuffer buffer) {
            buffer.append("tt");
            buffer.append(Long.toString(TextFragmentValue.this.treeInfo.getDocumentNumber()));
            buffer.append("t1");
        }

        @Override
        public String getSystemId() {
            return null;
        }

        @Override
        public String getBaseURI() {
            return TextFragmentValue.this.baseURI;
        }

        @Override
        public int compareOrder(NodeInfo other) {
            if (this == other) {
                return 0;
            }
            return 1;
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
            return new UntypedAtomicValue(TextFragmentValue.this.text);
        }

        @Override
        public AxisIterator iterateAxis(int axisNumber) {
            switch (axisNumber) {
                case 0: 
                case 9: 
                case 13: {
                    return SingleNodeIterator.makeIterator(TextFragmentValue.this);
                }
                case 1: {
                    NodeInfo[] nodes = new NodeInfo[]{this, TextFragmentValue.this};
                    return new ArrayIterator.OfNodes(nodes);
                }
                case 2: 
                case 3: 
                case 4: 
                case 6: 
                case 7: 
                case 8: 
                case 10: 
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
                case 0: 
                case 9: 
                case 13: {
                    return Navigator.filteredSingleton(TextFragmentValue.this, nodeTest);
                }
                case 1: {
                    boolean matchesDoc = nodeTest.test(TextFragmentValue.this);
                    boolean matchesText = nodeTest.test(this);
                    if (matchesDoc && matchesText) {
                        NodeInfo[] nodes = new NodeInfo[]{this, TextFragmentValue.this};
                        return new ArrayIterator.OfNodes(nodes);
                    }
                    if (matchesDoc) {
                        return SingleNodeIterator.makeIterator(TextFragmentValue.this);
                    }
                    if (matchesText) {
                        return SingleNodeIterator.makeIterator(this);
                    }
                    return EmptyIterator.ofNodes();
                }
                case 2: 
                case 3: 
                case 4: 
                case 6: 
                case 7: 
                case 8: 
                case 10: 
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
            return TextFragmentValue.this;
        }

        @Override
        public NodeInfo getRoot() {
            return TextFragmentValue.this;
        }

        @Override
        public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
            out.characters(TextFragmentValue.this.text, locationId, 0);
        }
    }
}

