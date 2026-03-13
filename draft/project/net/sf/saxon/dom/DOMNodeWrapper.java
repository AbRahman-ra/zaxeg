/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Predicate;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.util.SteppingNavigator;
import net.sf.saxon.tree.util.SteppingNode;
import net.sf.saxon.tree.wrapper.AbstractNodeWrapper;
import net.sf.saxon.tree.wrapper.SiblingCountingNode;
import net.sf.saxon.type.UType;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMNodeWrapper
extends AbstractNodeWrapper
implements SiblingCountingNode,
SteppingNode<DOMNodeWrapper> {
    protected Node node;
    protected short nodeKind;
    private DOMNodeWrapper parent;
    protected DocumentWrapper docWrapper;
    protected int index;
    protected int span = 1;
    private NamespaceBinding[] localNamespaces = null;
    private NamespaceMap inScopeNamespaces = null;

    protected DOMNodeWrapper(Node node, DocumentWrapper docWrapper, DOMNodeWrapper parent, int index) {
        this.node = node;
        this.parent = parent;
        this.index = index;
        this.docWrapper = docWrapper;
    }

    protected static DOMNodeWrapper makeWrapper(Node node, DocumentWrapper docWrapper) {
        if (node == null) {
            throw new NullPointerException("NodeWrapper#makeWrapper: Node must not be null");
        }
        if (docWrapper == null) {
            throw new NullPointerException("NodeWrapper#makeWrapper: DocumentWrapper must not be null");
        }
        return DOMNodeWrapper.makeWrapper(node, docWrapper, null, -1);
    }

    protected static DOMNodeWrapper makeWrapper(Node node, DocumentWrapper docWrapper, DOMNodeWrapper parent, int index) {
        DOMNodeWrapper wrapper;
        switch (node.getNodeType()) {
            case 9: 
            case 11: {
                wrapper = (DOMNodeWrapper)docWrapper.getRootNode();
                if (wrapper != null) break;
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = (short)9;
                break;
            }
            case 1: {
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = 1;
                break;
            }
            case 2: {
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = (short)2;
                break;
            }
            case 3: {
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = (short)3;
                break;
            }
            case 4: {
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = (short)3;
                break;
            }
            case 8: {
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = (short)8;
                break;
            }
            case 7: {
                wrapper = new DOMNodeWrapper(node, docWrapper, parent, index);
                wrapper.nodeKind = (short)7;
                break;
            }
            case 5: {
                throw new IllegalStateException("DOM contains entity reference nodes, which Saxon does not support. The DOM should be built using the expandEntityReferences() option");
            }
            default: {
                throw new IllegalArgumentException("Unsupported node type in DOM! " + node.getNodeType() + " instance " + node);
            }
        }
        wrapper.treeInfo = docWrapper;
        return wrapper;
    }

    @Override
    public DocumentWrapper getTreeInfo() {
        return (DocumentWrapper)this.treeInfo;
    }

    @Override
    public Node getUnderlyingNode() {
        return this.node;
    }

    @Override
    public int getNodeKind() {
        return this.nodeKind;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DOMNodeWrapper)) {
            return false;
        }
        if (this.docWrapper.domLevel3) {
            Node node = this.docWrapper.docNode;
            synchronized (node) {
                return this.node.isSameNode(((DOMNodeWrapper)other).node);
            }
        }
        DOMNodeWrapper ow = (DOMNodeWrapper)other;
        return this.getNodeKind() == ow.getNodeKind() && this.equalOrNull(this.getLocalPart(), ow.getLocalPart()) && this.getSiblingPosition() == ow.getSiblingPosition() && this.getParent().equals(ow.getParent());
    }

    private boolean equalOrNull(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int compareOrder(NodeInfo other) {
        if (other instanceof DOMNodeWrapper && this.docWrapper.domLevel3) {
            if (this.equals(other)) {
                return 0;
            }
            try {
                Node node = this.docWrapper.docNode;
                synchronized (node) {
                    short relationship = this.node.compareDocumentPosition(((DOMNodeWrapper)other).node);
                    if ((relationship & 0xA) != 0) {
                        return 1;
                    }
                    if ((relationship & 0x14) != 0) {
                        return -1;
                    }
                }
            } catch (DOMException dOMException) {
                // empty catch block
            }
        }
        if (other instanceof SiblingCountingNode) {
            return Navigator.compareOrder(this, (SiblingCountingNode)other);
        }
        return -other.compareOrder(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CharSequence getStringValueCS() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            switch (this.nodeKind) {
                case 1: 
                case 9: {
                    NodeList children1 = this.node.getChildNodes();
                    FastStringBuffer sb1 = new FastStringBuffer(16);
                    DOMNodeWrapper.expandStringValue(children1, sb1);
                    return sb1;
                }
                case 2: {
                    return DOMNodeWrapper.emptyIfNull(((Attr)this.node).getValue());
                }
                case 3: {
                    if (this.span == 1) {
                        return DOMNodeWrapper.emptyIfNull(this.node.getNodeValue());
                    }
                    FastStringBuffer fsb = new FastStringBuffer(64);
                    Node textNode = this.node;
                    for (int i = 0; i < this.span; ++i) {
                        fsb.append(DOMNodeWrapper.emptyIfNull(textNode.getNodeValue()));
                        textNode = textNode.getNextSibling();
                    }
                    return fsb.condense();
                }
                case 7: 
                case 8: {
                    return DOMNodeWrapper.emptyIfNull(this.node.getNodeValue());
                }
            }
            return "";
        }
    }

    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    public static void expandStringValue(NodeList list, FastStringBuffer sb) {
        int len = list.getLength();
        block5: for (int i = 0; i < len; ++i) {
            Node child = list.item(i);
            switch (child.getNodeType()) {
                case 1: {
                    DOMNodeWrapper.expandStringValue(child.getChildNodes(), sb);
                    continue block5;
                }
                case 7: 
                case 8: {
                    continue block5;
                }
                case 10: {
                    continue block5;
                }
                default: {
                    sb.append(DOMNodeWrapper.emptyIfNull(child.getNodeValue()));
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getLocalPart() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            switch (this.getNodeKind()) {
                case 1: 
                case 2: {
                    return DOMNodeWrapper.getLocalName(this.node);
                }
                case 7: {
                    return this.node.getNodeName();
                }
            }
            return "";
        }
    }

    public static String getLocalName(Node node) {
        String s = node.getLocalName();
        if (s == null) {
            String n = node.getNodeName();
            int colon = n.indexOf(58);
            if (colon >= 0) {
                return n.substring(colon + 1);
            }
            return n;
        }
        return s;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getURI() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            if (this.nodeKind == 1) {
                return DOMNodeWrapper.getElementURI((Element)this.node);
            }
            if (this.nodeKind == 2) {
                return DOMNodeWrapper.getAttributeURI((Attr)this.node);
            }
            return "";
        }
    }

    private static String getElementURI(Element element) {
        String attName;
        String uri = element.getNamespaceURI();
        if (uri != null) {
            return uri;
        }
        String displayName = element.getNodeName();
        int colon = displayName.indexOf(58);
        String string = attName = colon < 0 ? "xmlns" : "xmlns:" + displayName.substring(0, colon);
        if (attName.equals("xmlns:xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        Node node = element;
        do {
            if (!node.hasAttribute(attName)) continue;
            return node.getAttribute(attName);
        } while ((node = node.getParentNode()) != null && node.getNodeType() == 1);
        if (colon < 0) {
            return "";
        }
        throw new IllegalStateException("Undeclared namespace prefix in element name " + displayName + " in DOM input");
    }

    private static String getAttributeURI(Attr attr) {
        String uri = attr.getNamespaceURI();
        if (uri != null) {
            return uri;
        }
        String displayName = attr.getNodeName();
        int colon = displayName.indexOf(58);
        if (colon < 0) {
            return "";
        }
        String attName = "xmlns:" + displayName.substring(0, colon);
        if (attName.equals("xmlns:xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        Node node = attr.getOwnerElement();
        do {
            if (!node.hasAttribute(attName)) continue;
            return node.getAttribute(attName);
        } while ((node = node.getParentNode()) != null && node.getNodeType() == 1);
        throw new IllegalStateException("Undeclared namespace prefix in attribute name " + displayName + " in DOM input");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getPrefix() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            int kind = this.getNodeKind();
            if (kind == 1 || kind == 2) {
                String name = this.node.getNodeName();
                int colon = name.indexOf(58);
                if (colon < 0) {
                    return "";
                }
                return name.substring(0, colon);
            }
            return "";
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getDisplayName() {
        switch (this.nodeKind) {
            case 1: 
            case 2: 
            case 7: {
                Node node = this.docWrapper.docNode;
                synchronized (node) {
                    return this.node.getNodeName();
                }
            }
        }
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DOMNodeWrapper getParent() {
        if (this.parent == null) {
            Node node = this.docWrapper.docNode;
            synchronized (node) {
                switch (this.getNodeKind()) {
                    case 2: {
                        this.parent = DOMNodeWrapper.makeWrapper(((Attr)this.node).getOwnerElement(), this.docWrapper);
                        break;
                    }
                    default: {
                        Node p = this.node.getParentNode();
                        if (p == null) {
                            return null;
                        }
                        this.parent = DOMNodeWrapper.makeWrapper(p, this.docWrapper);
                    }
                }
            }
        }
        return this.parent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getSiblingPosition() {
        if (this.index == -1) {
            Node node = this.docWrapper.docNode;
            synchronized (node) {
                switch (this.nodeKind) {
                    case 1: 
                    case 3: 
                    case 7: 
                    case 8: {
                        int ix = 0;
                        Node start = this.node;
                        while (true) {
                            if ((start = start.getPreviousSibling()) == null) {
                                this.index = ix;
                                return ix;
                            }
                            ++ix;
                        }
                    }
                    case 2: {
                        int ix = 0;
                        AxisIterator iter = this.parent.iterateAxis(2);
                        while (true) {
                            NodeInfo n;
                            if ((n = iter.next()) == null || Navigator.haveSameName(this, n)) {
                                this.index = ix;
                                return ix;
                            }
                            ++ix;
                        }
                    }
                    case 13: {
                        int ix = 0;
                        AxisIterator iter = this.parent.iterateAxis(8);
                        while (true) {
                            NodeInfo n;
                            if ((n = iter.next()) == null || Navigator.haveSameName(this, n)) {
                                this.index = ix;
                                return ix;
                            }
                            ++ix;
                        }
                    }
                }
                this.index = 0;
                return this.index;
            }
        }
        return this.index;
    }

    @Override
    protected AxisIterator iterateAttributes(Predicate<? super NodeInfo> nodeTest) {
        AxisIterator iter = new AttributeEnumeration(this);
        if (nodeTest != AnyNodeTest.getInstance()) {
            iter = new Navigator.AxisFilter(iter, nodeTest);
        }
        return iter;
    }

    private boolean isElementOnly(Predicate<? super NodeInfo> nodeTest) {
        return nodeTest instanceof NodeTest && ((NodeTest)nodeTest).getUType() == UType.ELEMENT;
    }

    @Override
    protected AxisIterator iterateChildren(Predicate<? super NodeInfo> nodeTest) {
        boolean elementOnly = this.isElementOnly(nodeTest);
        AxisIterator iter = new Navigator.EmptyTextFilter(new ChildEnumeration(this, true, true, elementOnly));
        if (nodeTest != AnyNodeTest.getInstance()) {
            iter = new Navigator.AxisFilter(iter, nodeTest);
        }
        return iter;
    }

    @Override
    protected AxisIterator iterateSiblings(Predicate<? super NodeInfo> nodeTest, boolean forwards) {
        boolean elementOnly = this.isElementOnly(nodeTest);
        AxisIterator iter = new Navigator.EmptyTextFilter(new ChildEnumeration(this, false, forwards, elementOnly));
        if (nodeTest != AnyNodeTest.getInstance()) {
            iter = new Navigator.AxisFilter(iter, nodeTest);
        }
        return iter;
    }

    @Override
    protected AxisIterator iterateDescendants(Predicate<? super NodeInfo> nodeTest, boolean includeSelf) {
        return new SteppingNavigator.DescendantAxisIterator<DOMNodeWrapper>(this, includeSelf, nodeTest);
    }

    @Override
    public String getAttributeValue(String uri, String local) {
        NameTest test = new NameTest(2, uri, local, this.getNamePool());
        AxisIterator iterator = this.iterateAxis(2, test);
        NodeInfo attribute = iterator.next();
        if (attribute == null) {
            return null;
        }
        return attribute.getStringValue();
    }

    @Override
    public NodeInfo getRoot() {
        return this.docWrapper.getRootNode();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean hasChildNodes() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            return this.node.getNodeType() != 2 && this.node.hasChildNodes();
        }
    }

    @Override
    public void generateId(FastStringBuffer buffer) {
        Navigator.appendSequentialKey(this, buffer, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] buffer) {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            if (this.node.getNodeType() == 1) {
                if (this.localNamespaces != null) {
                    return this.localNamespaces;
                }
                Element elem = (Element)this.node;
                NamedNodeMap atts = elem.getAttributes();
                if (atts == null) {
                    this.localNamespaces = NamespaceBinding.EMPTY_ARRAY;
                    return NamespaceBinding.EMPTY_ARRAY;
                }
                int count = 0;
                int attsLen = atts.getLength();
                for (int i = 0; i < attsLen; ++i) {
                    Attr att = (Attr)atts.item(i);
                    String attName = att.getName();
                    if (attName.equals("xmlns")) {
                        ++count;
                        continue;
                    }
                    if (!attName.startsWith("xmlns:")) continue;
                    ++count;
                }
                if (count == 0) {
                    this.localNamespaces = NamespaceBinding.EMPTY_ARRAY;
                    return NamespaceBinding.EMPTY_ARRAY;
                }
                NamespaceBinding[] result = buffer == null || count > buffer.length ? new NamespaceBinding[count] : buffer;
                int n = 0;
                for (int i = 0; i < attsLen; ++i) {
                    String uri;
                    String prefix;
                    Attr att = (Attr)atts.item(i);
                    String attName = att.getName();
                    if (attName.equals("xmlns")) {
                        prefix = "";
                        uri = att.getValue();
                        result[n++] = new NamespaceBinding(prefix, uri);
                        continue;
                    }
                    if (!attName.startsWith("xmlns:")) continue;
                    prefix = attName.substring(6);
                    uri = att.getValue();
                    result[n++] = new NamespaceBinding(prefix, uri);
                }
                if (count < result.length) {
                    result[count] = null;
                }
                this.localNamespaces = Arrays.copyOf(result, result.length);
                return result;
            }
            return null;
        }
    }

    @Override
    public NamespaceMap getAllNamespaces() {
        if (this.getNodeKind() == 1) {
            if (this.inScopeNamespaces != null) {
                return this.inScopeNamespaces;
            }
            DOMNodeWrapper parent = this.getParent();
            NamespaceMap nsMap = parent != null && parent.getNodeKind() == 1 ? parent.getAllNamespaces() : NamespaceMap.emptyMap();
            Element elem = (Element)this.node;
            NamedNodeMap atts = elem.getAttributes();
            if (atts != null) {
                int attsLen = atts.getLength();
                for (int i = 0; i < attsLen; ++i) {
                    Attr att = (Attr)atts.item(i);
                    String attName = att.getName();
                    if (!attName.startsWith("xmlns")) continue;
                    if (attName.length() == 5) {
                        nsMap = nsMap.bind("", att.getValue());
                        continue;
                    }
                    if (attName.charAt(5) != ':') continue;
                    nsMap = nsMap.bind(attName.substring(6), att.getValue());
                }
            }
            this.inScopeNamespaces = nsMap;
            return this.inScopeNamespaces;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isId() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            return this.node instanceof Attr && ((Attr)this.node).isId();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DOMNodeWrapper getNextSibling() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            Node currNode = this.node;
            for (int i = 0; i < this.span; ++i) {
                currNode = currNode.getNextSibling();
            }
            if (currNode != null) {
                short type = currNode.getNodeType();
                if (type == 10) {
                    currNode = currNode.getNextSibling();
                } else if (type == 3 || type == 4) {
                    return this.spannedWrapper(currNode);
                }
                return DOMNodeWrapper.makeWrapper(currNode, this.docWrapper);
            }
            return null;
        }
    }

    private DOMNodeWrapper spannedWrapper(Node currNode) {
        Node currText = currNode;
        int thisSpan = 1;
        while ((currText = currText.getNextSibling()) != null && (currText.getNodeType() == 3 || currText.getNodeType() == 4)) {
            ++thisSpan;
        }
        DOMNodeWrapper spannedText = DOMNodeWrapper.makeWrapper(currNode, this.docWrapper);
        spannedText.span = thisSpan;
        return spannedText;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DOMNodeWrapper getFirstChild() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            Node currNode = this.node.getFirstChild();
            if (currNode != null) {
                if (currNode.getNodeType() == 10) {
                    currNode = currNode.getNextSibling();
                }
                if (currNode.getNodeType() == 3 || currNode.getNodeType() == 4) {
                    return this.spannedWrapper(currNode);
                }
                return DOMNodeWrapper.makeWrapper(currNode, this.docWrapper);
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DOMNodeWrapper getPreviousSibling() {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            Node currNode = this.node.getPreviousSibling();
            if (currNode != null) {
                short type = currNode.getNodeType();
                if (type == 10) {
                    return null;
                }
                if (type == 3 || type == 4) {
                    Node prev;
                    int span = 1;
                    while ((prev = currNode.getPreviousSibling()) != null && (prev.getNodeType() == 3 || prev.getNodeType() == 4)) {
                        ++span;
                        currNode = prev;
                    }
                    DOMNodeWrapper wrapper = DOMNodeWrapper.makeWrapper(currNode, this.docWrapper);
                    wrapper.span = span;
                    return wrapper;
                }
                return DOMNodeWrapper.makeWrapper(currNode, this.docWrapper);
            }
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public DOMNodeWrapper getSuccessorElement(DOMNodeWrapper anchor, String uri, String local) {
        Node node = this.docWrapper.docNode;
        synchronized (node) {
            Node stop = anchor == null ? null : anchor.node;
            Node next = this.node;
            while ((next = DOMNodeWrapper.getSuccessorNode(next, stop)) != null && (next.getNodeType() != 1 || local != null && !local.equals(DOMNodeWrapper.getLocalName(next)) || uri != null && !uri.equals(DOMNodeWrapper.getElementURI((Element)next)))) {
            }
            if (next == null) {
                return null;
            }
            return DOMNodeWrapper.makeWrapper(next, this.docWrapper);
        }
    }

    private static Node getSuccessorNode(Node start, Node anchor) {
        if (start.hasChildNodes()) {
            return start.getFirstChild();
        }
        if (anchor != null && start.isSameNode(anchor)) {
            return null;
        }
        Node p = start;
        do {
            Node s;
            if ((s = p.getNextSibling()) == null) continue;
            return s;
        } while ((p = p.getParentNode()) != null && (anchor == null || !p.isSameNode(anchor)));
        return null;
    }

    private final class ChildEnumeration
    implements AxisIterator,
    LookaheadIterator {
        private final DOMNodeWrapper start;
        private final DOMNodeWrapper commonParent;
        private final boolean downwards;
        private final boolean forwards;
        private final boolean elementsOnly;
        NodeList childNodes;
        private int childNodesLength;
        private int ix;
        private int currentSpan;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public ChildEnumeration(DOMNodeWrapper start, boolean downwards, boolean forwards, boolean elementsOnly) {
            Node node = start.docWrapper.docNode;
            synchronized (node) {
                this.start = start;
                this.downwards = downwards;
                this.forwards = forwards;
                this.elementsOnly = elementsOnly;
                this.currentSpan = 1;
                this.commonParent = downwards ? start : start.getParent();
                this.childNodes = this.commonParent.node.getChildNodes();
                this.childNodesLength = this.childNodes.getLength();
                if (downwards) {
                    this.currentSpan = 1;
                    this.ix = forwards ? -1 : this.childNodesLength;
                } else {
                    this.ix = start.getSiblingPosition();
                    this.currentSpan = start.span;
                }
            }
        }

        private int skipPrecedingTextNodes() {
            Node node;
            short kind;
            int count;
            for (count = 0; this.ix >= count && ((kind = (node = this.childNodes.item(this.ix - count)).getNodeType()) == 3 || kind == 4); ++count) {
            }
            return count == 0 ? 1 : count;
        }

        private int skipFollowingTextNodes() {
            Node node;
            short kind;
            int count = 0;
            int pos = this.ix;
            int len = this.childNodesLength;
            while (pos < len && ((kind = (node = this.childNodes.item(pos)).getNodeType()) == 3 || kind == 4)) {
                ++pos;
                ++count;
            }
            return count == 0 ? 1 : count;
        }

        @Override
        public boolean hasNext() {
            if (this.forwards) {
                return this.ix + this.currentSpan < this.childNodesLength;
            }
            return this.ix > 0;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public NodeInfo next() {
            Node node = this.start.docWrapper.docNode;
            synchronized (node) {
                Node currentDomNode;
                block11: while (true) {
                    if (this.forwards) {
                        this.ix += this.currentSpan;
                        if (this.ix >= this.childNodesLength) {
                            return null;
                        }
                        this.currentSpan = this.skipFollowingTextNodes();
                        currentDomNode = this.childNodes.item(this.ix);
                        switch (currentDomNode.getNodeType()) {
                            case 10: {
                                continue block11;
                            }
                            case 1: {
                                break;
                            }
                            default: {
                                if (this.elementsOnly) continue block11;
                            }
                        }
                        DOMNodeWrapper wrapper = DOMNodeWrapper.makeWrapper(currentDomNode, DOMNodeWrapper.this.docWrapper, this.commonParent, this.ix);
                        wrapper.span = this.currentSpan;
                        return wrapper;
                    }
                    --this.ix;
                    if (this.ix < 0) {
                        return null;
                    }
                    this.currentSpan = this.skipPrecedingTextNodes();
                    this.ix -= this.currentSpan - 1;
                    currentDomNode = this.childNodes.item(this.ix);
                    switch (currentDomNode.getNodeType()) {
                        case 10: {
                            continue block11;
                        }
                        case 1: {
                            break block11;
                        }
                        default: {
                            if (!this.elementsOnly) break block11;
                            continue block11;
                        }
                    }
                    break;
                }
                DOMNodeWrapper wrapper = DOMNodeWrapper.makeWrapper(currentDomNode, DOMNodeWrapper.this.docWrapper, this.commonParent, this.ix);
                wrapper.span = this.currentSpan;
                return wrapper;
            }
        }

        @Override
        public EnumSet<SequenceIterator.Property> getProperties() {
            return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
        }
    }

    private final class AttributeEnumeration
    implements AxisIterator,
    LookaheadIterator {
        private final ArrayList<Node> attList = new ArrayList(10);
        private int ix = 0;
        private final DOMNodeWrapper start;
        private DOMNodeWrapper current;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public AttributeEnumeration(DOMNodeWrapper start) {
            Node node = start.docWrapper.docNode;
            synchronized (node) {
                this.start = start;
                NamedNodeMap atts = start.node.getAttributes();
                if (atts != null) {
                    int attsLen = atts.getLength();
                    for (int i = 0; i < attsLen; ++i) {
                        String name = atts.item(i).getNodeName();
                        if (name.startsWith("xmlns") && (name.length() == 5 || name.charAt(5) == ':')) continue;
                        this.attList.add(atts.item(i));
                    }
                }
                this.ix = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return this.ix < this.attList.size();
        }

        @Override
        public NodeInfo next() {
            if (this.ix >= this.attList.size()) {
                return null;
            }
            this.current = DOMNodeWrapper.makeWrapper(this.attList.get(this.ix), DOMNodeWrapper.this.docWrapper, this.start, this.ix);
            ++this.ix;
            return this.current;
        }

        @Override
        public EnumSet<SequenceIterator.Property> getProperties() {
            return EnumSet.of(SequenceIterator.Property.LOOKAHEAD);
        }
    }
}

