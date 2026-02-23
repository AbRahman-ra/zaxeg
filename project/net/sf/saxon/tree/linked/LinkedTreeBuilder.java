/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.BuilderMonitor;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.event.ReceivingContentHandler;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.CommentImpl;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.LinkedBuilderMonitor;
import net.sf.saxon.tree.linked.NodeFactory;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.ParentNodeImpl;
import net.sf.saxon.tree.linked.ProcInstImpl;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class LinkedTreeBuilder
extends Builder {
    private ParentNodeImpl currentNode;
    private NodeFactory nodeFactory;
    private int[] size = new int[100];
    private int depth = 0;
    private ArrayList<NodeImpl[]> arrays = new ArrayList(20);
    private Stack<NamespaceMap> namespaceStack = new Stack();
    private boolean allocateSequenceNumbers = true;
    private int nextNodeNumber = 1;
    private boolean mutable;

    public LinkedTreeBuilder(PipelineConfiguration pipe) {
        super(pipe);
        this.nodeFactory = DefaultNodeFactory.THE_INSTANCE;
    }

    public LinkedTreeBuilder(PipelineConfiguration pipe, boolean mutable) {
        super(pipe);
        this.mutable = mutable;
        this.nodeFactory = DefaultNodeFactory.THE_INSTANCE;
    }

    @Override
    public NodeInfo getCurrentRoot() {
        NodeInfo physicalRoot = this.currentRoot;
        if (physicalRoot instanceof DocumentImpl && ((DocumentImpl)physicalRoot).isImaginary()) {
            return ((DocumentImpl)physicalRoot).getDocumentElement();
        }
        return physicalRoot;
    }

    @Override
    public void reset() {
        super.reset();
        this.currentNode = null;
        this.nodeFactory = DefaultNodeFactory.THE_INSTANCE;
        this.depth = 0;
        this.allocateSequenceNumbers = true;
        this.nextNodeNumber = 1;
    }

    public void setAllocateSequenceNumbers(boolean allocate) {
        this.allocateSequenceNumbers = allocate;
    }

    public void setNodeFactory(NodeFactory factory) {
        this.nodeFactory = factory;
    }

    @Override
    public void open() {
        this.started = true;
        this.depth = 0;
        this.size[this.depth] = 0;
        if (this.arrays == null) {
            this.arrays = new ArrayList(20);
        }
        super.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        DocumentImpl doc = new DocumentImpl();
        doc.setMutable(this.mutable);
        this.currentRoot = doc;
        doc.setSystemId(this.getSystemId());
        doc.setBaseURI(this.getBaseURI());
        doc.setConfiguration(this.config);
        this.currentNode = doc;
        this.depth = 0;
        this.size[this.depth] = 0;
        if (this.arrays == null) {
            this.arrays = new ArrayList(20);
        }
        doc.setRawSequenceNumber(0);
        if (this.lineNumbering) {
            doc.setLineNumbering();
        }
    }

    @Override
    public void endDocument() throws XPathException {
        this.currentNode.compact(this.size[this.depth]);
    }

    @Override
    public void close() throws XPathException {
        if (this.currentNode == null) {
            return;
        }
        this.currentNode.compact(this.size[this.depth]);
        this.currentNode = null;
        this.arrays = null;
        super.close();
        this.nodeFactory = DefaultNodeFactory.THE_INSTANCE;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap suppliedAttributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        int n;
        if (this.currentNode == null) {
            this.startDocument(0);
            ((DocumentImpl)this.currentRoot).setImaginary(true);
        }
        boolean isNilled = ReceiverOption.contains(properties, 16);
        this.namespaceStack.push(namespaces);
        boolean isTopWithinEntity = location instanceof ReceivingContentHandler.LocalLocator && ((ReceivingContentHandler.LocalLocator)location).levelInEntity == 0;
        AttributeInfo xmlId = suppliedAttributes.get("http://www.w3.org/XML/1998/namespace", "id");
        if (xmlId != null && Whitespace.containsWhitespace(xmlId.getValue())) {
            suppliedAttributes = suppliedAttributes.put(new AttributeInfo(xmlId.getNodeName(), xmlId.getType(), Whitespace.trim(xmlId.getValue()), xmlId.getLocation(), xmlId.getProperties()));
        }
        if (this.allocateSequenceNumbers) {
            int n2 = this.nextNodeNumber;
            n = n2;
            this.nextNodeNumber = n2 + 1;
        } else {
            n = -1;
        }
        ElementImpl elem = this.nodeFactory.makeElementNode(this.currentNode, elemName, type, isNilled, suppliedAttributes, this.namespaceStack.peek(), this.pipe, location, n);
        while (this.depth >= this.arrays.size()) {
            this.arrays.add(new NodeImpl[20]);
        }
        elem.setChildren(this.arrays.get(this.depth));
        int n3 = this.depth;
        int n4 = this.size[n3];
        this.size[n3] = n4 + 1;
        this.currentNode.addChild(elem, n4);
        if (this.depth >= this.size.length - 1) {
            this.size = Arrays.copyOf(this.size, this.size.length * 2);
        }
        this.size[++this.depth] = 0;
        if (this.currentNode instanceof TreeInfo) {
            ((DocumentImpl)this.currentNode).setDocumentElement(elem);
        }
        if (isTopWithinEntity) {
            this.currentNode.getPhysicalRoot().markTopWithinEntity(elem);
        }
        this.currentNode = elem;
    }

    @Override
    public void endElement() throws XPathException {
        this.currentNode.compact(this.size[this.depth]);
        --this.depth;
        this.currentNode = (ParentNodeImpl)this.currentNode.getParent();
        this.namespaceStack.pop();
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (chars.length() > 0) {
            NodeImpl prev = this.currentNode.getNthChild(this.size[this.depth] - 1);
            if (prev instanceof TextImpl) {
                ((TextImpl)prev).appendStringValue(chars.toString());
            } else {
                TextImpl n = this.nodeFactory.makeTextNode(this.currentNode, chars);
                int n2 = this.depth;
                int n3 = this.size[n2];
                this.size[n2] = n3 + 1;
                this.currentNode.addChild(n, n3);
            }
        }
    }

    @Override
    public void processingInstruction(String name, CharSequence remainder, Location locationId, int properties) {
        ProcInstImpl pi = new ProcInstImpl(name, remainder.toString());
        int n = this.depth;
        int n2 = this.size[n];
        this.size[n] = n2 + 1;
        this.currentNode.addChild(pi, n2);
        pi.setLocation(locationId.getSystemId(), locationId.getLineNumber(), locationId.getColumnNumber());
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        CommentImpl comment = new CommentImpl(chars.toString());
        int n = this.depth;
        int n2 = this.size[n];
        this.size[n] = n2 + 1;
        this.currentNode.addChild(comment, n2);
        comment.setLocation(locationId.getSystemId(), locationId.getLineNumber(), locationId.getColumnNumber());
    }

    public ParentNodeImpl getCurrentParentNode() {
        return this.currentNode;
    }

    public NodeImpl getCurrentLeafNode() {
        return this.currentNode.getLastChild();
    }

    public void graftElement(ElementImpl element) {
        int n = this.depth;
        int n2 = this.size[n];
        this.size[n] = n2 + 1;
        this.currentNode.addChild(element, n2);
    }

    @Override
    public void setUnparsedEntity(String name, String uri, String publicId) {
        if (((DocumentImpl)this.currentRoot).getUnparsedEntity(name) == null) {
            ((DocumentImpl)this.currentRoot).setUnparsedEntity(name, uri, publicId);
        }
    }

    @Override
    public BuilderMonitor getBuilderMonitor() {
        return new LinkedBuilderMonitor(this);
    }

    private static class DefaultNodeFactory
    implements NodeFactory {
        public static DefaultNodeFactory THE_INSTANCE = new DefaultNodeFactory();

        private DefaultNodeFactory() {
        }

        @Override
        public ElementImpl makeElementNode(NodeInfo parent, NodeName nodeName, SchemaType elementType, boolean isNilled, AttributeMap attlist, NamespaceMap namespaces, PipelineConfiguration pipe, Location locationId, int sequenceNumber) {
            ElementImpl e = new ElementImpl();
            e.setNamespaceMap(namespaces);
            e.initialise(nodeName, elementType, attlist, parent, sequenceNumber);
            if (isNilled) {
                e.setNilled();
            }
            if (locationId != Loc.NONE && sequenceNumber >= 0) {
                String baseURI = locationId.getSystemId();
                int lineNumber = locationId.getLineNumber();
                int columnNumber = locationId.getColumnNumber();
                e.setLocation(baseURI, lineNumber, columnNumber);
            }
            return e;
        }

        @Override
        public TextImpl makeTextNode(NodeInfo parent, CharSequence content) {
            return new TextImpl(content.toString());
        }
    }
}

