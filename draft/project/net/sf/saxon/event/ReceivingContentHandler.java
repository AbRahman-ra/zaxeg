/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.QuitParsingException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.BuiltInListType;
import net.sf.saxon.type.CastingTarget;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.value.Whitespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.LexicalHandler;

public class ReceivingContentHandler
implements ContentHandler,
LexicalHandler,
DTDHandler {
    private PipelineConfiguration pipe;
    private Receiver receiver;
    private boolean inDTD = false;
    private LocalLocator localLocator = new LocalLocator(Loc.NONE);
    private boolean lineNumbering;
    private Location lastTextNodeLocator;
    private char[] buffer = new char[512];
    private int charsUsed = 0;
    private CharSlice slice = new CharSlice(this.buffer, 0, 0);
    private Stack<NamespaceMap> namespaceStack = new Stack();
    private NamespaceMap currentNamespaceMap;
    private boolean ignoreIgnorable = false;
    private boolean retainDTDAttributeTypes = false;
    private boolean allowDisableOutputEscaping = false;
    private boolean escapingDisabled = false;
    private boolean afterStartTag = true;
    private final HashMap<String, HashMap<String, NodeName>> nameCache = new HashMap(10);
    private HashMap<String, NodeName> noNamespaceNameCache = new HashMap(10);
    private int defaultedAttributesAction = 0;
    private Stack<Integer> elementDepthWithinEntity;

    public ReceivingContentHandler() {
        this.currentNamespaceMap = NamespaceMap.emptyMap();
        this.namespaceStack.push(this.currentNamespaceMap);
    }

    public void reset() {
        this.pipe = null;
        this.receiver = null;
        this.ignoreIgnorable = false;
        this.retainDTDAttributeTypes = false;
        this.charsUsed = 0;
        this.slice.setLength(0);
        this.namespaceStack = new Stack();
        this.currentNamespaceMap = NamespaceMap.emptyMap();
        this.namespaceStack.push(this.currentNamespaceMap);
        this.localLocator = new LocalLocator(Loc.NONE);
        this.allowDisableOutputEscaping = false;
        this.escapingDisabled = false;
        this.lineNumbering = false;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public Receiver getReceiver() {
        return this.receiver;
    }

    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
        Configuration config = pipe.getConfiguration();
        this.ignoreIgnorable = pipe.getParseOptions().getSpaceStrippingRule() != NoElementsSpaceStrippingRule.getInstance();
        this.retainDTDAttributeTypes = config.getBooleanProperty(Feature.RETAIN_DTD_ATTRIBUTE_TYPES);
        if (!pipe.getParseOptions().isExpandAttributeDefaults()) {
            this.defaultedAttributesAction = -1;
        } else if (config.getBooleanProperty(Feature.MARK_DEFAULTED_ATTRIBUTES)) {
            this.defaultedAttributesAction = 1;
        }
        this.allowDisableOutputEscaping = config.getConfigurationProperty(Feature.USE_PI_DISABLE_OUTPUT_ESCAPING);
        this.lineNumbering = pipe.getParseOptions().isLineNumbering();
    }

    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    public Configuration getConfiguration() {
        return this.pipe.getConfiguration();
    }

    public void setIgnoreIgnorableWhitespace(boolean ignore) {
        this.ignoreIgnorable = ignore;
    }

    public boolean isIgnoringIgnorableWhitespace() {
        return this.ignoreIgnorable;
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            this.charsUsed = 0;
            this.currentNamespaceMap = NamespaceMap.emptyMap();
            this.namespaceStack = new Stack();
            this.namespaceStack.push(this.currentNamespaceMap);
            this.receiver.setPipelineConfiguration(this.pipe);
            String systemId = this.localLocator.getSystemId();
            if (systemId != null) {
                this.receiver.setSystemId(this.localLocator.getSystemId());
            }
            this.receiver.open();
            this.receiver.startDocument(0);
        } catch (QuitParsingException quit) {
            this.getPipelineConfiguration().getErrorReporter().report(new XmlProcessingException(quit).asWarning());
            throw new SAXException(quit);
        } catch (XPathException err) {
            throw new SAXException(err);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            this.flush(true);
            this.receiver.endDocument();
            this.receiver.close();
        } catch (ValidationException err) {
            err.setLocator(this.localLocator);
            throw new SAXException(err);
        } catch (QuitParsingException err) {
        } catch (XPathException err) {
            err.maybeSetLocation(this.localLocator);
            throw new SAXException(err);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.localLocator = new LocalLocator(locator);
        if (!this.lineNumbering) {
            this.lastTextNodeLocator = this.localLocator;
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        if (prefix.equals("xmlns")) {
            return;
        }
        this.currentNamespaceMap = this.currentNamespaceMap.bind(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }

    @Override
    public void startElement(String uri, String localname, String rawname, Attributes atts) throws SAXException {
        try {
            this.flush(true);
            int options = 524352;
            NodeName elementName = this.getNodeName(uri, localname, rawname);
            AttributeMap attributes = this.makeAttributeMap(atts, this.localLocator);
            this.receiver.startElement(elementName, Untyped.getInstance(), attributes, this.currentNamespaceMap, this.localLocator, options);
            ++this.localLocator.levelInEntity;
            this.namespaceStack.push(this.currentNamespaceMap);
            this.afterStartTag = true;
        } catch (XPathException err) {
            err.maybeSetLocation(this.localLocator);
            throw new SAXException(err);
        }
    }

    private AttributeMap makeAttributeMap(Attributes atts, Location location) throws SAXException {
        int length = atts.getLength();
        ArrayList<AttributeInfo> list = new ArrayList<AttributeInfo>(atts.getLength());
        for (int a = 0; a < length; ++a) {
            int properties = 64;
            String value = atts.getValue(a);
            String qname = atts.getQName(a);
            if (qname.startsWith("xmlns") && (qname.length() == 5 || qname.charAt(5) == ':')) continue;
            if (this.defaultedAttributesAction != 0 && atts instanceof Attributes2 && !((Attributes2)atts).isSpecified(qname)) {
                if (this.defaultedAttributesAction == -1) continue;
                properties |= 8;
            }
            NodeName attCode = this.getNodeName(atts.getURI(a), atts.getLocalName(a), atts.getQName(a));
            String type = atts.getType(a);
            CastingTarget typeCode = BuiltInAtomicType.UNTYPED_ATOMIC;
            if (this.retainDTDAttributeTypes) {
                switch (type) {
                    case "CDATA": {
                        break;
                    }
                    case "ID": {
                        typeCode = BuiltInAtomicType.ID;
                        break;
                    }
                    case "IDREF": {
                        typeCode = BuiltInAtomicType.IDREF;
                        break;
                    }
                    case "IDREFS": {
                        typeCode = BuiltInListType.IDREFS;
                        break;
                    }
                    case "NMTOKEN": {
                        typeCode = BuiltInAtomicType.NMTOKEN;
                        break;
                    }
                    case "NMTOKENS": {
                        typeCode = BuiltInListType.NMTOKENS;
                        break;
                    }
                    case "ENTITY": {
                        typeCode = BuiltInAtomicType.ENTITY;
                        break;
                    }
                    case "ENTITIES": {
                        typeCode = BuiltInListType.ENTITIES;
                    }
                }
            } else {
                switch (type) {
                    case "ID": {
                        properties |= 0x800;
                        break;
                    }
                    case "IDREF": {
                        properties |= 0x1000;
                        break;
                    }
                    case "IDREFS": {
                        properties |= 0x1000;
                    }
                }
            }
            list.add(new AttributeInfo(attCode, (SimpleType)((Object)typeCode), value, location, properties));
        }
        return AttributeMap.fromList(list);
    }

    private NodeName getNodeName(String uri, String localname, String rawname) throws SAXException {
        NodeName n;
        HashMap<String, NodeName> map2;
        if (rawname.isEmpty()) {
            throw new SAXException("Saxon requires an XML parser that reports the QName of each element");
        }
        if (localname.isEmpty()) {
            throw new SAXException("Parser configuration problem: namespace reporting is not enabled");
        }
        HashMap<String, NodeName> hashMap = map2 = uri.isEmpty() ? this.noNamespaceNameCache : this.nameCache.get(uri);
        if (map2 == null) {
            map2 = new HashMap(50);
            this.nameCache.put(uri, map2);
            if (uri.isEmpty()) {
                this.noNamespaceNameCache = map2;
            }
        }
        if ((n = map2.get(rawname)) == null) {
            if (uri.isEmpty()) {
                NoNamespaceName qn = new NoNamespaceName(localname);
                map2.put(rawname, qn);
                return qn;
            }
            String prefix = NameChecker.getPrefix(rawname);
            FingerprintedQName qn = new FingerprintedQName(prefix, uri, localname);
            map2.put(rawname, qn);
            return qn;
        }
        return n;
    }

    @Override
    public void endElement(String uri, String localname, String rawname) throws SAXException {
        try {
            this.flush(!this.afterStartTag);
            --this.localLocator.levelInEntity;
            this.receiver.endElement();
        } catch (ValidationException err) {
            err.maybeSetLocation(this.localLocator);
            if (!err.hasBeenReported()) {
                this.pipe.getErrorReporter().report(new XmlProcessingException(err));
            }
            err.setHasBeenReported(true);
            throw new SAXException(err);
        } catch (XPathException err) {
            err.maybeSetLocation(this.localLocator);
            throw new SAXException(err);
        }
        this.afterStartTag = false;
        this.namespaceStack.pop();
        this.currentNamespaceMap = this.namespaceStack.peek();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        while (this.charsUsed + length > this.buffer.length) {
            this.buffer = Arrays.copyOf(this.buffer, this.buffer.length * 2);
            this.slice = new CharSlice(this.buffer, 0, 0);
        }
        System.arraycopy(ch, start, this.buffer, this.charsUsed, length);
        this.charsUsed += length;
        if (this.lineNumbering) {
            this.lastTextNodeLocator = this.localLocator.saveLocation();
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
        if (!this.ignoreIgnorable) {
            this.characters(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String name, String remainder) throws SAXException {
        try {
            this.flush(true);
            if (!this.inDTD) {
                if (name == null) {
                    this.comment(remainder.toCharArray(), 0, remainder.length());
                } else {
                    if (!NameChecker.isValidNCName(name)) {
                        throw new SAXException("Invalid processing instruction name (" + name + ')');
                    }
                    if (this.allowDisableOutputEscaping) {
                        if (name.equals("javax.xml.transform.disable-output-escaping")) {
                            this.escapingDisabled = true;
                            return;
                        }
                        if (name.equals("javax.xml.transform.enable-output-escaping")) {
                            this.escapingDisabled = false;
                            return;
                        }
                    }
                    CharSequence data = remainder == null ? "" : Whitespace.removeLeadingWhitespace(remainder);
                    this.receiver.processingInstruction(name, data, this.localLocator, 0);
                }
            }
        } catch (XPathException err) {
            throw new SAXException(err);
        }
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        try {
            this.flush(true);
            if (!this.inDTD) {
                this.receiver.comment(new CharSlice(ch, start, length), this.localLocator, 0);
            }
        } catch (XPathException err) {
            throw new SAXException(err);
        }
    }

    private void flush(boolean compress) throws XPathException {
        if (this.charsUsed > 0) {
            this.slice.setLength(this.charsUsed);
            CharSequence cs = compress ? CompressedWhitespace.compress(this.slice) : this.slice;
            this.receiver.characters(cs, this.lastTextNodeLocator, this.escapingDisabled ? 1 : 1024);
            this.charsUsed = 0;
            this.escapingDisabled = false;
        }
    }

    @Override
    public void skippedEntity(String name) {
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) {
        this.inDTD = true;
    }

    @Override
    public void endDTD() {
        this.inDTD = false;
    }

    @Override
    public void startEntity(String name) {
        if (this.elementDepthWithinEntity == null) {
            this.elementDepthWithinEntity = new Stack();
        }
        this.elementDepthWithinEntity.push(this.localLocator.levelInEntity);
        this.localLocator.levelInEntity = 0;
    }

    @Override
    public void endEntity(String name) {
        this.localLocator.levelInEntity = this.elementDepthWithinEntity.pop();
    }

    @Override
    public void startCDATA() {
    }

    @Override
    public void endCDATA() {
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) {
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        String uri = systemId;
        if (this.localLocator != null) {
            try {
                String baseURI;
                URI suppliedURI = new URI(systemId);
                if (!suppliedURI.isAbsolute() && (baseURI = this.localLocator.getSystemId()) != null) {
                    uri = ResolveURI.makeAbsolute(systemId, baseURI).toString();
                }
            } catch (URISyntaxException suppliedURI) {
                // empty catch block
            }
        }
        try {
            this.receiver.setUnparsedEntity(name, uri, publicId);
        } catch (XPathException err) {
            throw new SAXException(err);
        }
    }

    public static class LocalLocator
    implements Location {
        private final Locator saxLocator;
        public int levelInEntity;

        LocalLocator(Locator saxLocator) {
            this.saxLocator = saxLocator;
            this.levelInEntity = 0;
        }

        @Override
        public String getSystemId() {
            return this.saxLocator.getSystemId();
        }

        @Override
        public String getPublicId() {
            return this.saxLocator.getPublicId();
        }

        @Override
        public int getLineNumber() {
            return this.saxLocator.getLineNumber();
        }

        @Override
        public int getColumnNumber() {
            return this.saxLocator.getColumnNumber();
        }

        @Override
        public Location saveLocation() {
            return new Loc(this.getSystemId(), this.getLineNumber(), this.getColumnNumber());
        }
    }
}

