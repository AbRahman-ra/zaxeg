/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.SingletonAttributeMap;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;

public class SequenceWrapper
extends SequenceReceiver {
    public static final String RESULT_NS = QueryResult.RESULT_NS;
    private ComplexContentOutputter out;
    private int depth = 0;
    private FingerprintedQName resultDocument;
    private FingerprintedQName resultElement;
    private FingerprintedQName resultAttribute;
    private FingerprintedQName resultText;
    private FingerprintedQName resultComment;
    private FingerprintedQName resultPI;
    private FingerprintedQName resultNamespace;
    private FingerprintedQName resultAtomicValue;
    private FingerprintedQName resultFunction;
    private FingerprintedQName resultArray;
    private FingerprintedQName resultMap;
    private FingerprintedQName resultExternalValue;
    private FingerprintedQName xsiType;
    private NamespaceMap namespaces;

    public SequenceWrapper(Receiver destination) {
        super(destination.getPipelineConfiguration());
        this.out = new ComplexContentOutputter(destination);
    }

    public ComplexContentOutputter getDestination() {
        return this.out;
    }

    private void startWrapper(NodeName name) throws XPathException {
        this.out.startElement(name, Untyped.getInstance(), Loc.NONE, 0);
        this.out.namespace("", "", 0);
        this.out.startContent();
    }

    private void endWrapper() throws XPathException {
        this.out.endElement();
    }

    @Override
    public void open() throws XPathException {
        FingerprintedQName resultSequence = new FingerprintedQName("result", RESULT_NS, "sequence");
        this.resultDocument = new FingerprintedQName("result", RESULT_NS, "document");
        this.resultElement = new FingerprintedQName("result", RESULT_NS, "element");
        this.resultAttribute = new FingerprintedQName("result", RESULT_NS, "attribute");
        this.resultText = new FingerprintedQName("result", RESULT_NS, "text");
        this.resultComment = new FingerprintedQName("result", RESULT_NS, "comment");
        this.resultPI = new FingerprintedQName("result", RESULT_NS, "processing-instruction");
        this.resultNamespace = new FingerprintedQName("result", RESULT_NS, "namespace");
        this.resultAtomicValue = new FingerprintedQName("result", RESULT_NS, "atomic-value");
        this.resultFunction = new FingerprintedQName("result", RESULT_NS, "function");
        this.resultArray = new FingerprintedQName("result", RESULT_NS, "array");
        this.resultMap = new FingerprintedQName("result", RESULT_NS, "map");
        this.resultExternalValue = new FingerprintedQName("result", RESULT_NS, "external-object");
        this.xsiType = new FingerprintedQName("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type");
        this.out.open();
        this.out.startDocument(0);
        this.namespaces = NamespaceMap.emptyMap().put("result", RESULT_NS).put("xs", "http://www.w3.org/2001/XMLSchema").put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        this.startWrapper(resultSequence);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.startWrapper(this.resultDocument);
        ++this.depth;
    }

    @Override
    public void endDocument() throws XPathException {
        this.endWrapper();
        --this.depth;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (this.depth++ == 0) {
            this.startWrapper(this.resultElement);
        }
        this.out.startElement(elemName, type, location, properties);
        this.out.namespace("", "", properties);
        for (AttributeInfo att : attributes) {
            this.out.attribute(att.getNodeName(), att.getType(), att.getValue(), att.getLocation(), att.getProperties());
        }
        this.out.startContent();
    }

    @Override
    public void endElement() throws XPathException {
        this.out.endElement();
        if (--this.depth == 0) {
            this.endWrapper();
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.depth == 0) {
            this.startWrapper(this.resultText);
            this.out.characters(chars, locationId, properties);
            this.endWrapper();
        } else {
            this.out.characters(chars, locationId, properties);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.depth == 0) {
            this.startWrapper(this.resultComment);
            this.out.comment(chars, locationId, properties);
            this.endWrapper();
        } else {
            this.out.comment(chars, locationId, properties);
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.depth == 0) {
            this.startWrapper(this.resultPI);
            this.out.processingInstruction(target, data, locationId, properties);
            this.endWrapper();
        } else {
            this.out.processingInstruction(target, data, locationId, properties);
        }
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item instanceof AtomicValue) {
            NamePool pool = this.getNamePool();
            this.out.startElement(this.resultAtomicValue, Untyped.getInstance(), Loc.NONE, 0);
            this.out.namespace("", "", 0);
            AtomicType type = ((AtomicValue)item).getItemType();
            StructuredQName name = type.getStructuredQName();
            String prefix = name.getPrefix();
            String localName = name.getLocalPart();
            String uri = name.getURI();
            if (prefix.isEmpty() && (prefix = pool.suggestPrefixForURI(uri)) == null) {
                prefix = "p" + uri.hashCode();
            }
            String displayName = prefix + ':' + localName;
            this.out.namespace("", "", 0);
            this.out.namespace(prefix, uri, 0);
            this.out.attribute(this.xsiType, BuiltInAtomicType.UNTYPED_ATOMIC, displayName, locationId, 0);
            this.out.startContent();
            this.out.characters(item.getStringValue(), locationId, 0);
            this.out.endElement();
        } else if (item instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)item;
            int kind = node.getNodeKind();
            if (kind == 2) {
                this.attribute(NameOfNode.makeName(node), (SimpleType)node.getSchemaType(), node.getStringValueCS(), Loc.NONE, 0);
            } else if (kind == 13) {
                this.namespace(new NamespaceBinding(node.getLocalPart(), node.getStringValue()), 0);
            } else {
                ((NodeInfo)item).copy(this, 6, locationId);
            }
        } else if (item instanceof Function) {
            if (item instanceof MapItem) {
                this.out.startElement(this.resultMap, Untyped.getInstance(), Loc.NONE, 0);
                this.out.startContent();
                this.out.characters(item.toShortString(), locationId, 0);
                this.out.endElement();
            } else if (item instanceof ArrayItem) {
                this.out.startElement(this.resultArray, Untyped.getInstance(), Loc.NONE, 0);
                this.out.startContent();
                this.out.characters(item.toShortString(), locationId, 0);
                this.out.endElement();
            } else {
                this.out.startElement(this.resultFunction, Untyped.getInstance(), Loc.NONE, 0);
                this.out.startContent();
                this.out.characters(((Function)item).getDescription(), locationId, 0);
                this.out.endElement();
            }
        } else if (item instanceof ObjectValue) {
            Object obj = ((ObjectValue)item).getObject();
            this.out.startElement(this.resultExternalValue, Untyped.getInstance(), Loc.NONE, 0);
            this.out.attribute(new NoNamespaceName("class"), BuiltInAtomicType.UNTYPED_ATOMIC, obj.getClass().getName(), Loc.NONE, 0);
            this.out.startContent();
            this.out.characters(obj.toString(), locationId, 0);
            this.out.endElement();
        }
    }

    @Override
    public void close() throws XPathException {
        this.endWrapper();
        this.out.endDocument();
        this.out.close();
    }

    @Override
    public boolean usesTypeAnnotations() {
        return true;
    }

    private void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location locationId, int properties) throws XPathException {
        SingletonAttributeMap atts = SingletonAttributeMap.of(new AttributeInfo(attName, typeCode, value.toString(), locationId, properties));
        NamespaceMap ns = NamespaceMap.emptyMap();
        if (!attName.hasURI("")) {
            ns = ns.put(attName.getPrefix(), attName.getURI());
        }
        this.out.startElement(this.resultAttribute, Untyped.getInstance(), atts, ns, Loc.NONE, 0);
        this.out.startContent();
        this.out.endElement();
    }

    private void namespace(NamespaceBindingSet namespaceBindings, int properties) throws XPathException {
        NamespaceMap ns = NamespaceMap.emptyMap();
        ns = ns.addAll(namespaceBindings);
        this.out.startElement(this.resultNamespace, Untyped.getInstance(), EmptyAttributeMap.getInstance(), ns, Loc.NONE, 0);
        this.out.startContent();
        this.out.endElement();
    }
}

