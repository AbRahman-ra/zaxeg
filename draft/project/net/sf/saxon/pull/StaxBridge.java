/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EntityDeclaration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.UnparsedEntity;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Whitespace;

public class StaxBridge
implements PullProvider {
    private XMLStreamReader reader;
    private AttributeMap attributes;
    private PipelineConfiguration pipe;
    private NamePool namePool;
    private HashMap<String, NodeName> nameCache = new HashMap();
    private Stack<NamespaceMap> namespaceStack = new Stack();
    private List unparsedEntities = null;
    PullProvider.Event currentEvent = PullProvider.Event.START_OF_INPUT;
    int depth = 0;
    boolean ignoreIgnorable = false;

    public StaxBridge() {
        this.namespaceStack.push(NamespaceMap.emptyMap());
    }

    public void setInputStream(String systemId, InputStream inputStream) throws XPathException {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setXMLReporter(new StaxErrorReporter());
            this.reader = factory.createXMLStreamReader(systemId, inputStream);
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    public void setXMLStreamReader(XMLStreamReader reader) {
        this.reader = reader;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = new PipelineConfiguration(pipe);
        this.namePool = pipe.getConfiguration().getNamePool();
        this.ignoreIgnorable = pipe.getConfiguration().getParseOptions().getSpaceStrippingRule() != NoElementsSpaceStrippingRule.getInstance();
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    public XMLStreamReader getXMLStreamReader() {
        return this.reader;
    }

    public NamePool getNamePool() {
        return this.pipe.getConfiguration().getNamePool();
    }

    @Override
    public PullProvider.Event next() throws XPathException {
        if (this.currentEvent == PullProvider.Event.START_OF_INPUT) {
            this.currentEvent = PullProvider.Event.START_DOCUMENT;
            return this.currentEvent;
        }
        if (this.currentEvent == PullProvider.Event.END_OF_INPUT || this.currentEvent == PullProvider.Event.END_DOCUMENT) {
            try {
                this.reader.close();
            } catch (XMLStreamException e) {
                throw new XPathException(e);
            }
            return PullProvider.Event.END_OF_INPUT;
        }
        try {
            if (this.reader.hasNext()) {
                int event = this.reader.next();
                this.currentEvent = this.translate(event);
                if (this.currentEvent == PullProvider.Event.START_ELEMENT) {
                    NamespaceMap nsMap = this.namespaceStack.peek();
                    int n = this.reader.getNamespaceCount();
                    for (int i = 0; i < n; ++i) {
                        String prefix = this.reader.getNamespacePrefix(i);
                        String uri = this.reader.getNamespaceURI(i);
                        nsMap = nsMap.bind(prefix == null ? "" : prefix, uri == null ? "" : uri);
                    }
                    this.namespaceStack.push(nsMap);
                    int attCount = this.reader.getAttributeCount();
                    if (attCount == 0) {
                        this.attributes = EmptyAttributeMap.getInstance();
                    } else {
                        ArrayList<AttributeInfo> attList = new ArrayList<AttributeInfo>();
                        NamePool pool = this.getNamePool();
                        for (int i = 0; i < attCount; ++i) {
                            QName name = this.reader.getAttributeName(i);
                            FingerprintedQName fName = new FingerprintedQName(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), pool);
                            String value = this.reader.getAttributeValue(i);
                            AttributeInfo att = new AttributeInfo(fName, BuiltInAtomicType.UNTYPED_ATOMIC, value, Loc.NONE, 0);
                            attList.add(att);
                        }
                        this.attributes = AttributeMap.fromList(attList);
                    }
                } else if (this.currentEvent == PullProvider.Event.END_ELEMENT) {
                    this.namespaceStack.pop();
                }
            } else {
                this.currentEvent = PullProvider.Event.END_OF_INPUT;
            }
        } catch (XMLStreamException e) {
            int c;
            String message = e.getMessage();
            if (message.startsWith("ParseError at") && (c = message.indexOf("\nMessage: ")) > 0) {
                message = message.substring(c + 10);
            }
            XPathException err = new XPathException("Error reported by XML parser: " + message, e);
            err.setErrorCode("SXXP0003");
            err.setLocator(this.translateLocation(e.getLocation()));
            throw err;
        }
        return this.currentEvent;
    }

    private PullProvider.Event translate(int event) throws XPathException {
        switch (event) {
            case 10: {
                return PullProvider.Event.ATTRIBUTE;
            }
            case 12: {
                return PullProvider.Event.TEXT;
            }
            case 4: {
                if (this.depth == 0 && this.reader.isWhiteSpace()) {
                    return this.next();
                }
                return PullProvider.Event.TEXT;
            }
            case 5: {
                return PullProvider.Event.COMMENT;
            }
            case 11: {
                this.unparsedEntities = (List)this.reader.getProperty("javax.xml.stream.entities");
                return this.next();
            }
            case 8: {
                return PullProvider.Event.END_DOCUMENT;
            }
            case 2: {
                --this.depth;
                return PullProvider.Event.END_ELEMENT;
            }
            case 15: {
                return this.next();
            }
            case 9: {
                return this.next();
            }
            case 13: {
                return PullProvider.Event.NAMESPACE;
            }
            case 14: {
                return this.next();
            }
            case 3: {
                return PullProvider.Event.PROCESSING_INSTRUCTION;
            }
            case 6: {
                if (this.depth == 0) {
                    return this.next();
                }
                if (this.ignoreIgnorable) {
                    return this.next();
                }
                return PullProvider.Event.TEXT;
            }
            case 7: {
                return this.next();
            }
            case 1: {
                ++this.depth;
                return PullProvider.Event.START_ELEMENT;
            }
        }
        throw new IllegalStateException("Unknown StAX event " + event);
    }

    @Override
    public PullProvider.Event current() {
        return this.currentEvent;
    }

    @Override
    public AttributeMap getAttributes() {
        return this.attributes;
    }

    @Override
    public NamespaceBinding[] getNamespaceDeclarations() {
        int n = this.reader.getNamespaceCount();
        if (n == 0) {
            return NamespaceBinding.EMPTY_ARRAY;
        }
        NamespaceBinding[] bindings = new NamespaceBinding[n];
        for (int i = 0; i < n; ++i) {
            String uri;
            String prefix = this.reader.getNamespacePrefix(i);
            if (prefix == null) {
                prefix = "";
            }
            if ((uri = this.reader.getNamespaceURI(i)) == null) {
                uri = "";
            }
            bindings[i] = new NamespaceBinding(prefix, uri);
        }
        return bindings;
    }

    @Override
    public PullProvider.Event skipToMatchingEnd() throws XPathException {
        switch (this.currentEvent) {
            case START_DOCUMENT: {
                this.currentEvent = PullProvider.Event.END_DOCUMENT;
                return this.currentEvent;
            }
            case START_ELEMENT: {
                try {
                    int skipDepth = 0;
                    while (this.reader.hasNext()) {
                        int event = this.reader.next();
                        if (event == 1) {
                            ++skipDepth;
                            continue;
                        }
                        if (event != 2 || skipDepth-- != 0) continue;
                        this.currentEvent = PullProvider.Event.END_ELEMENT;
                        return this.currentEvent;
                    }
                } catch (XMLStreamException e) {
                    throw new XPathException(e);
                }
                throw new IllegalStateException("Element start has no matching element end");
            }
        }
        throw new IllegalStateException("Cannot call skipToMatchingEnd() except when at start of element or document");
    }

    @Override
    public void close() {
        try {
            this.reader.close();
        } catch (XMLStreamException xMLStreamException) {
            // empty catch block
        }
    }

    @Override
    public NodeName getNodeName() {
        if (this.currentEvent == PullProvider.Event.START_ELEMENT || this.currentEvent == PullProvider.Event.END_ELEMENT) {
            String local = this.reader.getLocalName();
            String uri = this.reader.getNamespaceURI();
            NodeName cached = this.nameCache.get(local);
            if (cached != null && cached.hasURI(uri == null ? "" : uri) && cached.getPrefix().equals(this.reader.getPrefix())) {
                return cached;
            }
            int fp = this.namePool.allocateFingerprint(uri, local);
            cached = uri == null ? new NoNamespaceName(local, fp) : new FingerprintedQName(this.reader.getPrefix(), uri, local, fp);
            this.nameCache.put(local, cached);
            return cached;
        }
        if (this.currentEvent == PullProvider.Event.PROCESSING_INSTRUCTION) {
            String local = this.reader.getPITarget();
            return new NoNamespaceName(local);
        }
        throw new IllegalStateException();
    }

    @Override
    public CharSequence getStringValue() throws XPathException {
        switch (this.currentEvent) {
            case TEXT: {
                CharSlice cs = new CharSlice(this.reader.getTextCharacters(), this.reader.getTextStart(), this.reader.getTextLength());
                return CompressedWhitespace.compress(cs);
            }
            case COMMENT: {
                return new CharSlice(this.reader.getTextCharacters(), this.reader.getTextStart(), this.reader.getTextLength());
            }
            case PROCESSING_INSTRUCTION: {
                String s = this.reader.getPIData();
                return Whitespace.removeLeadingWhitespace(s);
            }
            case START_ELEMENT: {
                FastStringBuffer combinedText = null;
                try {
                    int depth = 0;
                    while (this.reader.hasNext()) {
                        int event = this.reader.next();
                        if (event == 4) {
                            if (combinedText == null) {
                                combinedText = new FastStringBuffer(64);
                            }
                            combinedText.append(this.reader.getTextCharacters(), this.reader.getTextStart(), this.reader.getTextLength());
                            continue;
                        }
                        if (event == 1) {
                            ++depth;
                            continue;
                        }
                        if (event != 2 || depth-- != 0) continue;
                        this.currentEvent = PullProvider.Event.END_ELEMENT;
                        if (combinedText != null) {
                            return combinedText.condense();
                        }
                        return "";
                    }
                    break;
                } catch (XMLStreamException e) {
                    throw new XPathException(e);
                }
            }
        }
        throw new IllegalStateException("getStringValue() called when current event is " + (Object)((Object)this.currentEvent));
    }

    @Override
    public AtomicValue getAtomicValue() {
        throw new IllegalStateException();
    }

    @Override
    public SchemaType getSchemaType() {
        if (this.currentEvent == PullProvider.Event.START_ELEMENT) {
            return Untyped.getInstance();
        }
        if (this.currentEvent == PullProvider.Event.ATTRIBUTE) {
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        }
        return null;
    }

    @Override
    public net.sf.saxon.s9api.Location getSourceLocator() {
        return this.translateLocation(this.reader.getLocation());
    }

    private Loc translateLocation(Location location) {
        if (location == null) {
            return Loc.NONE;
        }
        return new Loc(location.getSystemId(), location.getLineNumber(), location.getColumnNumber());
    }

    @Override
    public List<UnparsedEntity> getUnparsedEntities() {
        if (this.unparsedEntities == null) {
            return null;
        }
        ArrayList<UnparsedEntity> list = new ArrayList<UnparsedEntity>(this.unparsedEntities.size());
        for (Object ent : this.unparsedEntities) {
            String name = null;
            String systemId = null;
            String publicId = null;
            String baseURI = null;
            if (ent instanceof EntityDeclaration) {
                EntityDeclaration ed = (EntityDeclaration)ent;
                name = ed.getName();
                systemId = ed.getSystemId();
                publicId = ed.getPublicId();
                baseURI = ed.getBaseURI();
            } else if (ent.getClass().getName().equals("com.ctc.wstx.ent.UnparsedExtEntity")) {
                try {
                    Class<?> woodstoxClass = ent.getClass();
                    Class[] noArgClasses = new Class[]{};
                    Object[] noArgs = new Object[]{};
                    Method method = woodstoxClass.getMethod("getName", noArgClasses);
                    name = (String)method.invoke(ent, noArgs);
                    method = woodstoxClass.getMethod("getSystemId", noArgClasses);
                    systemId = (String)method.invoke(ent, noArgs);
                    method = woodstoxClass.getMethod("getPublicId", noArgClasses);
                    publicId = (String)method.invoke(ent, noArgs);
                    method = woodstoxClass.getMethod("getBaseURI", noArgClasses);
                    baseURI = (String)method.invoke(ent, noArgs);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException woodstoxClass) {
                    // empty catch block
                }
            }
            if (name == null) continue;
            if (baseURI != null && systemId != null) {
                try {
                    systemId = ResolveURI.makeAbsolute(systemId, baseURI).toString();
                } catch (URISyntaxException woodstoxClass) {
                    // empty catch block
                }
            }
            UnparsedEntity ue = new UnparsedEntity();
            ue.setName(name);
            ue.setSystemId(systemId);
            ue.setPublicId(publicId);
            ue.setBaseURI(baseURI);
            list.add(ue);
        }
        return list;
    }

    private class StaxErrorReporter
    implements XMLReporter {
        private StaxErrorReporter() {
        }

        @Override
        public void report(String message, String errorType, Object relatedInformation, Location location) {
            XmlProcessingIncident err = new XmlProcessingIncident("Error reported by XML parser: " + message + " (" + errorType + ')');
            err.setLocation(StaxBridge.this.translateLocation(location));
            StaxBridge.this.pipe.getErrorReporter().report(err);
        }
    }
}

