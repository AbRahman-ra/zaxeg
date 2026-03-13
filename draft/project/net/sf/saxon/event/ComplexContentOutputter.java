/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import javax.xml.transform.Result;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.NoOpenStartTagException;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.event.RegularSequenceChecker;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ExternalObject;

public final class ComplexContentOutputter
extends Outputter
implements Receiver,
Result {
    private Receiver nextReceiver;
    private NodeName pendingStartTag = null;
    private int level = -1;
    private boolean[] currentLevelIsDocument = new boolean[20];
    private final List<AttributeInfo> pendingAttributes = new ArrayList<AttributeInfo>();
    private NamespaceMap pendingNSMap;
    private final Stack<NamespaceMap> inheritedNamespaces = new Stack();
    private SchemaType currentSimpleType = null;
    private int startElementProperties;
    private Location startElementLocationId = Loc.NONE;
    private HostLanguage hostLanguage = HostLanguage.XSLT;
    private RegularSequenceChecker.State state = RegularSequenceChecker.State.Initial;
    private boolean previousAtomic = false;

    public ComplexContentOutputter(Receiver next) {
        PipelineConfiguration pipe = next.getPipelineConfiguration();
        this.setPipelineConfiguration(pipe);
        this.setReceiver(next);
        Objects.requireNonNull(pipe);
        this.setHostLanguage(pipe.getHostLanguage());
        this.inheritedNamespaces.push(NamespaceMap.emptyMap());
    }

    public static ComplexContentOutputter makeComplexContentReceiver(Receiver receiver, ParseOptions options) {
        boolean validate;
        String systemId = receiver.getSystemId();
        boolean bl = validate = options != null && options.getSchemaValidationMode() != 3;
        if (validate) {
            Configuration config = receiver.getPipelineConfiguration().getConfiguration();
            receiver = config.getDocumentValidator(receiver, systemId, options, null);
        }
        ComplexContentOutputter result = new ComplexContentOutputter(receiver);
        result.setSystemId(systemId);
        return result;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        if (this.pipelineConfiguration != pipe) {
            this.pipelineConfiguration = pipe;
            if (this.nextReceiver != null) {
                this.nextReceiver.setPipelineConfiguration(pipe);
            }
        }
    }

    @Override
    public void setSystemId(String systemId) {
        super.setSystemId(systemId);
        this.nextReceiver.setSystemId(systemId);
    }

    public void setHostLanguage(HostLanguage language) {
        this.hostLanguage = language;
    }

    public void setReceiver(Receiver receiver) {
        this.nextReceiver = receiver;
    }

    public Receiver getReceiver() {
        return this.nextReceiver;
    }

    @Override
    public void open() throws XPathException {
        this.nextReceiver.open();
        this.previousAtomic = false;
        this.state = RegularSequenceChecker.State.Open;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        ++this.level;
        if (this.level == 0) {
            this.nextReceiver.startDocument(properties);
        } else if (this.state == RegularSequenceChecker.State.StartTag) {
            this.startContent();
        }
        this.previousAtomic = false;
        if (this.currentLevelIsDocument.length < this.level + 1) {
            this.currentLevelIsDocument = Arrays.copyOf(this.currentLevelIsDocument, this.level * 2);
        }
        this.currentLevelIsDocument[this.level] = true;
        this.state = RegularSequenceChecker.State.Content;
    }

    @Override
    public void endDocument() throws XPathException {
        if (this.level == 0) {
            this.nextReceiver.endDocument();
        }
        this.previousAtomic = false;
        --this.level;
        this.state = this.level < 0 ? RegularSequenceChecker.State.Open : RegularSequenceChecker.State.Content;
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        this.nextReceiver.setUnparsedEntity(name, systemID, publicID);
    }

    @Override
    public void characters(CharSequence s, Location locationId, int properties) throws XPathException {
        if (this.level >= 0) {
            this.previousAtomic = false;
            if (s == null) {
                return;
            }
            int len = s.length();
            if (len == 0) {
                return;
            }
            if (this.state == RegularSequenceChecker.State.StartTag) {
                this.startContent();
            }
        }
        this.nextReceiver.characters(s, locationId, properties);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType typeCode, Location location, int properties) throws XPathException {
        ++this.level;
        if (this.state == RegularSequenceChecker.State.StartTag) {
            this.startContent();
        }
        this.startElementProperties = properties;
        this.startElementLocationId = location.saveLocation();
        this.pendingAttributes.clear();
        this.pendingNSMap = NamespaceMap.emptyMap();
        this.pendingStartTag = elemName;
        this.currentSimpleType = typeCode;
        this.previousAtomic = false;
        if (this.currentLevelIsDocument.length < this.level + 1) {
            this.currentLevelIsDocument = Arrays.copyOf(this.currentLevelIsDocument, this.level * 2);
        }
        this.currentLevelIsDocument[this.level] = false;
        this.state = RegularSequenceChecker.State.StartTag;
    }

    @Override
    public void namespace(String prefix, String namespaceUri, int properties) throws XPathException {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(namespaceUri);
        if (ReceiverOption.contains(properties, 64)) {
            this.pendingNSMap = this.pendingNSMap.put(prefix, namespaceUri);
        } else if (this.level >= 0) {
            String uri;
            if (this.state != RegularSequenceChecker.State.StartTag) {
                throw NoOpenStartTagException.makeNoOpenStartTagException(13, prefix, this.hostLanguage, this.currentLevelIsDocument[this.level], this.startElementLocationId);
            }
            boolean elementIsInNullNamespace = this.pendingStartTag.hasURI("");
            if (prefix.isEmpty() && !namespaceUri.isEmpty() && elementIsInNullNamespace) {
                XPathException err = new XPathException("Cannot output a namespace node for the default namespace (" + namespaceUri + ") when the element is in no namespace");
                err.setErrorCode(this.hostLanguage == HostLanguage.XSLT ? "XTDE0440" : "XQDY0102");
                throw err;
            }
            boolean rejectDuplicates = ReceiverOption.contains(properties, 32);
            if (rejectDuplicates && (uri = this.pendingNSMap.getURI(prefix)) != null && !uri.equals(namespaceUri)) {
                XPathException err = new XPathException("Cannot create two namespace nodes with the same prefix mapped to different URIs (prefix=\"" + prefix + "\", URIs=(\"" + uri + "\", \"" + namespaceUri + "\"))");
                err.setErrorCode(this.hostLanguage == HostLanguage.XSLT ? "XTDE0430" : "XQDY0102");
                throw err;
            }
            this.pendingNSMap = this.pendingNSMap.put(prefix, namespaceUri);
        } else {
            Orphan orphan = new Orphan(this.getConfiguration());
            orphan.setNodeKind((short)13);
            orphan.setNodeName(new NoNamespaceName(prefix));
            orphan.setStringValue(namespaceUri);
            this.nextReceiver.append(orphan, Loc.NONE, properties);
        }
        this.previousAtomic = false;
    }

    @Override
    public void namespaces(NamespaceBindingSet bindings, int properties) throws XPathException {
        if (bindings instanceof NamespaceMap && this.pendingNSMap.isEmpty() && ReceiverOption.contains(properties, 64)) {
            this.pendingNSMap = (NamespaceMap)bindings;
        } else {
            super.namespaces(bindings, properties);
        }
    }

    @Override
    public void attribute(NodeName attName, SimpleType typeCode, CharSequence value, Location locationId, int properties) throws XPathException {
        if (this.level >= 0 && this.state != RegularSequenceChecker.State.StartTag) {
            NoOpenStartTagException err = NoOpenStartTagException.makeNoOpenStartTagException(2, attName.getDisplayName(), this.hostLanguage, this.currentLevelIsDocument[this.level], this.startElementLocationId);
            err.setLocator(locationId);
            throw err;
        }
        AttributeInfo attInfo = new AttributeInfo(attName, typeCode, value.toString(), locationId, properties);
        if (this.level >= 0 && !ReceiverOption.contains(properties, 0x100000)) {
            for (int a = 0; a < this.pendingAttributes.size(); ++a) {
                if (!this.pendingAttributes.get(a).getNodeName().equals(attName)) continue;
                if (this.hostLanguage == HostLanguage.XSLT) {
                    this.pendingAttributes.set(a, attInfo);
                    return;
                }
                XPathException err = new XPathException("Cannot create an element having two attributes with the same name: " + Err.wrap(attName.getDisplayName(), 2));
                err.setErrorCode("XQDY0025");
                throw err;
            }
        }
        if (this.level == 0 && !typeCode.equals(BuiltInAtomicType.UNTYPED_ATOMIC) && this.currentLevelIsDocument[0] && typeCode.isNamespaceSensitive()) {
            XPathException err = new XPathException("Cannot copy attributes whose type is namespace-sensitive (QName or NOTATION): " + Err.wrap(attName.getDisplayName(), 2));
            err.setErrorCode(this.hostLanguage == HostLanguage.XSLT ? "XTTE0950" : "XQTY0086");
            throw err;
        }
        if (this.level < 0) {
            Orphan orphan = new Orphan(this.getConfiguration());
            orphan.setNodeKind((short)2);
            orphan.setNodeName(attName);
            orphan.setTypeAnnotation(typeCode);
            orphan.setStringValue(value);
            this.nextReceiver.append(orphan, locationId, properties);
        }
        this.pendingAttributes.add(attInfo);
        this.previousAtomic = false;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        NamespaceMap ns2;
        boolean inherit;
        if (this.state == RegularSequenceChecker.State.StartTag) {
            this.startContent();
        }
        ++this.level;
        this.startElementLocationId = location.saveLocation();
        if (this.currentLevelIsDocument.length < this.level + 1) {
            this.currentLevelIsDocument = Arrays.copyOf(this.currentLevelIsDocument, this.level * 2);
        }
        this.currentLevelIsDocument[this.level] = false;
        if (elemName.hasURI("") && !namespaces.getDefaultNamespace().isEmpty()) {
            namespaces = namespaces.remove("");
        }
        boolean bl = inherit = !ReceiverOption.contains(properties, 128);
        if (inherit) {
            NamespaceMap inherited = this.inheritedNamespaces.peek();
            if (!inherited.getDefaultNamespace().isEmpty() && elemName.getURI().isEmpty()) {
                inherited = inherited.remove("");
            }
            ns2 = inherited.putAll(namespaces);
            if (ReceiverOption.contains(properties, 131072)) {
                this.inheritedNamespaces.push(inherited);
            } else {
                this.inheritedNamespaces.push(ns2);
            }
        } else {
            ns2 = namespaces;
            this.inheritedNamespaces.push(NamespaceMap.emptyMap());
        }
        boolean refuseInheritedNamespaces = ReceiverOption.contains(properties, 65536);
        NamespaceMap ns3 = refuseInheritedNamespaces ? namespaces : ns2;
        this.nextReceiver.startElement(elemName, type, attributes, ns3, location, properties);
        this.state = RegularSequenceChecker.State.Content;
    }

    private NodeName checkProposedPrefix(NodeName nodeName, int seq) {
        String nodePrefix = nodeName.getPrefix();
        String nodeURI = nodeName.getURI();
        if (nodeURI.isEmpty()) {
            return nodeName;
        }
        String uri = this.pendingNSMap.getURI(nodePrefix);
        if (uri == null) {
            this.pendingNSMap = this.pendingNSMap.put(nodePrefix, nodeURI);
            return nodeName;
        }
        if (nodeURI.equals(uri)) {
            return nodeName;
        }
        String newPrefix = this.getSubstitutePrefix(nodePrefix, nodeURI, seq);
        FingerprintedQName newName = new FingerprintedQName(newPrefix, nodeURI, nodeName.getLocalPart());
        this.pendingNSMap = this.pendingNSMap.put(newPrefix, nodeURI);
        return newName;
    }

    private String getSubstitutePrefix(String prefix, String uri, int seq) {
        if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
            return "xml";
        }
        return prefix + '_' + seq;
    }

    @Override
    public void endElement() throws XPathException {
        if (this.state == RegularSequenceChecker.State.StartTag) {
            this.startContent();
        } else {
            this.pendingStartTag = null;
        }
        this.nextReceiver.endElement();
        --this.level;
        this.previousAtomic = false;
        this.state = this.level < 0 ? RegularSequenceChecker.State.Open : RegularSequenceChecker.State.Content;
        this.inheritedNamespaces.pop();
    }

    @Override
    public void comment(CharSequence comment, Location locationId, int properties) throws XPathException {
        if (this.level >= 0) {
            if (this.state == RegularSequenceChecker.State.StartTag) {
                this.startContent();
            }
            this.previousAtomic = false;
        }
        this.nextReceiver.comment(comment, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.level >= 0) {
            if (this.state == RegularSequenceChecker.State.StartTag) {
                this.startContent();
            }
            this.previousAtomic = false;
        }
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (this.level >= 0) {
            this.decompose(item, locationId, copyNamespaces);
        } else {
            this.nextReceiver.append(item, locationId, copyNamespaces);
        }
    }

    @Override
    public CharSequenceConsumer getStringReceiver(final boolean asTextNode) {
        if (this.level >= 0) {
            return new CharSequenceConsumer(){

                @Override
                public void open() throws XPathException {
                    if (ComplexContentOutputter.this.previousAtomic && !asTextNode) {
                        ComplexContentOutputter.this.characters(" ", Loc.NONE, 0);
                    }
                }

                @Override
                public CharSequenceConsumer cat(CharSequence chars) throws XPathException {
                    ComplexContentOutputter.this.characters(chars, Loc.NONE, 0);
                    return this;
                }

                @Override
                public void close() {
                    ComplexContentOutputter.this.previousAtomic = !asTextNode;
                }
            };
        }
        return super.getStringReceiver(asTextNode);
    }

    @Override
    public void close() throws XPathException {
        this.nextReceiver.close();
        this.previousAtomic = false;
        this.state = RegularSequenceChecker.State.Final;
    }

    @Override
    public void startContent() throws XPathException {
        NamespaceMap inherited;
        if (this.state != RegularSequenceChecker.State.StartTag) {
            return;
        }
        NodeName elcode = this.checkProposedPrefix(this.pendingStartTag, 0);
        int props = this.startElementProperties | 0x40;
        for (int a = 0; a < this.pendingAttributes.size(); ++a) {
            NodeName newName;
            NodeName oldName = this.pendingAttributes.get(a).getNodeName();
            if (oldName.hasURI("") || (newName = this.checkProposedPrefix(oldName, a + 1)) == oldName) continue;
            AttributeInfo newInfo = this.pendingAttributes.get(a).withNodeName(newName);
            this.pendingAttributes.set(a, newInfo);
        }
        NamespaceMap namespaceMap = inherited = this.inheritedNamespaces.isEmpty() ? NamespaceMap.emptyMap() : this.inheritedNamespaces.peek();
        if (!ReceiverOption.contains(this.startElementProperties, 65536)) {
            this.pendingNSMap = inherited.putAll(this.pendingNSMap);
        }
        if (this.pendingStartTag.hasURI("") && !this.pendingNSMap.getDefaultNamespace().isEmpty()) {
            this.pendingNSMap = this.pendingNSMap.remove("");
        }
        AttributeMap attributes = AttributeMap.fromList(this.pendingAttributes);
        this.nextReceiver.startElement(elcode, this.currentSimpleType, attributes, this.pendingNSMap, this.startElementLocationId, props);
        boolean inherit = !ReceiverOption.contains(this.startElementProperties, 128);
        this.inheritedNamespaces.push(inherit ? this.pendingNSMap : inherited);
        this.pendingAttributes.clear();
        this.pendingNSMap = NamespaceMap.emptyMap();
        this.previousAtomic = false;
        this.state = RegularSequenceChecker.State.Content;
    }

    @Override
    public boolean usesTypeAnnotations() {
        return this.nextReceiver.usesTypeAnnotations();
    }

    protected void flatten(ArrayItem array, Location locationId, int copyNamespaces) throws XPathException {
        for (Sequence sequence : array.members()) {
            sequence.iterate().forEachOrFail(it -> this.append(it, locationId, copyNamespaces));
        }
    }

    protected void decompose(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item != null) {
            if (item instanceof AtomicValue || item instanceof ExternalObject) {
                if (this.previousAtomic) {
                    this.characters(" ", locationId, 0);
                }
                this.characters(item.getStringValueCS(), locationId, 0);
                this.previousAtomic = true;
            } else if (item instanceof ArrayItem) {
                this.flatten((ArrayItem)item, locationId, copyNamespaces);
            } else {
                if (item instanceof Function) {
                    String thing = item instanceof MapItem ? "map" : "function item";
                    String errorCode = this.getErrorCodeForDecomposingFunctionItems();
                    if (errorCode.startsWith("SENR")) {
                        throw new XPathException("Cannot serialize a " + thing + " using this output method", errorCode, locationId);
                    }
                    throw new XPathException("Cannot add a " + thing + " to an XDM node tree", errorCode, locationId);
                }
                NodeInfo node = (NodeInfo)item;
                switch (node.getNodeKind()) {
                    case 3: {
                        int options = 0;
                        if (node instanceof Orphan && ((Orphan)node).isDisableOutputEscaping()) {
                            options = 1;
                        }
                        this.characters(item.getStringValueCS(), locationId, options);
                        break;
                    }
                    case 2: {
                        if (((SimpleType)node.getSchemaType()).isNamespaceSensitive()) {
                            XPathException err = new XPathException("Cannot copy attributes whose type is namespace-sensitive (QName or NOTATION): " + Err.wrap(node.getDisplayName(), 2));
                            err.setErrorCode(this.getPipelineConfiguration().isXSLT() ? "XTTE0950" : "XQTY0086");
                            throw err;
                        }
                        this.attribute(NameOfNode.makeName(node), (SimpleType)node.getSchemaType(), node.getStringValue(), locationId, 0);
                        break;
                    }
                    case 13: {
                        this.namespace(node.getLocalPart(), node.getStringValue(), 0);
                        break;
                    }
                    case 9: {
                        this.startDocument(0);
                        for (NodeInfo nodeInfo : node.children()) {
                            this.append(nodeInfo, locationId, copyNamespaces);
                        }
                        this.endDocument();
                        break;
                    }
                    default: {
                        int copyOptions = 4;
                        if (ReceiverOption.contains(copyNamespaces, 524288)) {
                            copyOptions |= 2;
                        }
                        ((NodeInfo)item).copy(this, copyOptions, locationId);
                    }
                }
                this.previousAtomic = false;
            }
        }
    }

    protected String getErrorCodeForDecomposingFunctionItems() {
        return this.getPipelineConfiguration().isXSLT() ? "XTDE0450" : "XQTY0105";
    }
}

