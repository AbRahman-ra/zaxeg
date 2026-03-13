/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.IntPredicate;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.StandardURIChecker;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.pull.NamespaceContextImpl;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;

public class StreamWriterToReceiver
implements XMLStreamWriter {
    private static boolean DEBUG = false;
    private StartTag pendingTag = null;
    private Receiver receiver;
    private Configuration config;
    private IntPredicate charChecker;
    private boolean isChecking = false;
    private int depth = -1;
    private boolean isEmptyElement;
    private NamespaceReducer inScopeNamespaces;
    private Stack<List<NamespaceBinding>> setPrefixes = new Stack();
    private NamespaceContext rootNamespaceContext = null;

    public StreamWriterToReceiver(Receiver receiver) {
        PipelineConfiguration pipe = receiver.getPipelineConfiguration();
        this.inScopeNamespaces = new NamespaceReducer(receiver);
        this.receiver = this.inScopeNamespaces;
        this.config = pipe.getConfiguration();
        this.charChecker = pipe.getConfiguration().getValidCharacterChecker();
        this.setPrefixes.push(new ArrayList());
        this.rootNamespaceContext = new NamespaceContextImpl(new NamespaceResolver(){

            @Override
            public String getURIForPrefix(String prefix, boolean useDefault) {
                return null;
            }

            @Override
            public Iterator<String> iteratePrefixes() {
                List e = Collections.emptyList();
                return e.iterator();
            }
        });
    }

    public Receiver getReceiver() {
        return this.receiver;
    }

    public void setCheckValues(boolean check) {
        this.isChecking = check;
    }

    public boolean isCheckValues() {
        return this.isChecking;
    }

    private void flushStartTag() throws XMLStreamException {
        if (this.depth == -1) {
            this.writeStartDocument();
        }
        if (this.pendingTag != null) {
            try {
                this.completeTriple(this.pendingTag.elementName, false);
                for (Triple t : this.pendingTag.attributes) {
                    this.completeTriple(t, true);
                }
                NodeName elemName = this.pendingTag.elementName.uri.isEmpty() ? new NoNamespaceName(this.pendingTag.elementName.local) : new FingerprintedQName(this.pendingTag.elementName.prefix, this.pendingTag.elementName.uri, this.pendingTag.elementName.local);
                NamespaceMap nsMap = NamespaceMap.emptyMap();
                if (!this.pendingTag.elementName.uri.isEmpty()) {
                    nsMap = nsMap.put(this.pendingTag.elementName.prefix, this.pendingTag.elementName.uri);
                }
                for (Triple t : this.pendingTag.namespaces) {
                    if (t.prefix == null) {
                        t.prefix = "";
                    }
                    if (t.uri == null) {
                        t.uri = "";
                    }
                    if (t.uri.isEmpty()) continue;
                    nsMap = nsMap.put(t.prefix, t.uri);
                }
                AttributeMap attributes = EmptyAttributeMap.getInstance();
                for (Triple t : this.pendingTag.attributes) {
                    NodeName attName;
                    if (t.uri.isEmpty()) {
                        attName = new NoNamespaceName(t.local);
                    } else {
                        attName = new FingerprintedQName(t.prefix, t.uri, t.local);
                        nsMap = nsMap.put(t.prefix, t.uri);
                    }
                    attributes = attributes.put(new AttributeInfo(attName, BuiltInAtomicType.UNTYPED_ATOMIC, t.value, Loc.NONE, 0));
                }
                this.receiver.startElement(elemName, Untyped.getInstance(), attributes, nsMap, Loc.NONE, 0);
                this.pendingTag = null;
                if (this.isEmptyElement) {
                    this.isEmptyElement = false;
                    --this.depth;
                    this.setPrefixes.pop();
                    this.receiver.endElement();
                }
            } catch (XPathException e) {
                throw new XMLStreamException(e);
            }
        }
    }

    private void completeTriple(Triple t, boolean isAttribute) throws XMLStreamException {
        if (t.local == null) {
            throw new XMLStreamException("Local name of " + (isAttribute ? "Attribute" : "Element") + " is missing");
        }
        if (this.isChecking && !this.isValidNCName(t.local)) {
            throw new XMLStreamException("Local name of " + (isAttribute ? "Attribute" : "Element") + Err.wrap(t.local) + " is invalid");
        }
        if (t.prefix == null) {
            t.prefix = "";
        }
        if (t.uri == null) {
            t.uri = "";
        }
        if (this.isChecking && !t.uri.isEmpty() && this.isInvalidURI(t.uri)) {
            throw new XMLStreamException("Namespace URI " + Err.wrap(t.local) + " is invalid");
        }
        if (t.prefix.isEmpty() && !t.uri.isEmpty()) {
            t.prefix = this.getPrefixForUri(t.uri);
        }
    }

    private String getDefaultNamespace() {
        for (Triple t : this.pendingTag.namespaces) {
            if (t.prefix != null && !t.prefix.isEmpty()) continue;
            return t.uri;
        }
        return this.inScopeNamespaces.getURIForPrefix("", true);
    }

    private String getUriForPrefix(String prefix) {
        for (Triple t : this.pendingTag.namespaces) {
            if (!prefix.equals(t.prefix)) continue;
            return t.uri;
        }
        return this.inScopeNamespaces.getURIForPrefix(prefix, false);
    }

    private String getPrefixForUri(String uri) {
        for (Triple t : this.pendingTag.namespaces) {
            if (!uri.equals(t.uri)) continue;
            return t.prefix == null ? "" : t.prefix;
        }
        String setPrefix = this.getPrefix(uri);
        if (setPrefix != null) {
            return setPrefix;
        }
        Iterator<String> prefixes = this.inScopeNamespaces.iteratePrefixes();
        while (prefixes.hasNext()) {
            String p = prefixes.next();
            if (!this.inScopeNamespaces.getURIForPrefix(p, false).equals(uri)) continue;
            return p;
        }
        return "";
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        if (DEBUG) {
            System.err.println("StartElement " + localName);
        }
        this.checkNonNull(localName);
        this.setPrefixes.push(new ArrayList());
        this.flushStartTag();
        ++this.depth;
        this.pendingTag = new StartTag();
        this.pendingTag.elementName.local = localName;
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        if (DEBUG) {
            System.err.println("StartElement Q{" + namespaceURI + "}" + localName);
        }
        this.checkNonNull(namespaceURI);
        this.checkNonNull(localName);
        this.setPrefixes.push(new ArrayList());
        this.flushStartTag();
        ++this.depth;
        this.pendingTag = new StartTag();
        this.pendingTag.elementName.local = localName;
        this.pendingTag.elementName.uri = namespaceURI;
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        if (DEBUG) {
            System.err.println("StartElement " + prefix + "=Q{" + namespaceURI + "}" + localName);
        }
        this.checkNonNull(prefix);
        this.checkNonNull(localName);
        this.checkNonNull(namespaceURI);
        this.setPrefixes.push(new ArrayList());
        this.flushStartTag();
        ++this.depth;
        this.pendingTag = new StartTag();
        this.pendingTag.elementName.local = localName;
        this.pendingTag.elementName.uri = namespaceURI;
        this.pendingTag.elementName.prefix = prefix;
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        this.checkNonNull(namespaceURI);
        this.checkNonNull(localName);
        this.flushStartTag();
        this.writeStartElement(namespaceURI, localName);
        this.isEmptyElement = true;
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        this.checkNonNull(prefix);
        this.checkNonNull(localName);
        this.checkNonNull(namespaceURI);
        this.flushStartTag();
        this.writeStartElement(prefix, localName, namespaceURI);
        this.isEmptyElement = true;
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        this.checkNonNull(localName);
        this.flushStartTag();
        this.writeStartElement(localName);
        this.isEmptyElement = true;
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        if (DEBUG) {
            System.err.println("EndElement" + this.depth);
        }
        if (this.depth <= 0) {
            throw new IllegalStateException("writeEndElement with no matching writeStartElement");
        }
        try {
            this.flushStartTag();
            this.setPrefixes.pop();
            this.receiver.endElement();
            --this.depth;
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        if (this.depth == -1) {
            throw new IllegalStateException("writeEndDocument with no matching writeStartDocument");
        }
        try {
            this.flushStartTag();
            while (this.depth > 0) {
                this.writeEndElement();
            }
            this.receiver.endDocument();
            this.depth = -1;
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
    }

    @Override
    public void close() throws XMLStreamException {
        if (this.depth >= 0) {
            this.writeEndDocument();
        }
        try {
            this.receiver.close();
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void writeAttribute(String localName, String value) {
        this.checkNonNull(localName);
        this.checkNonNull(value);
        if (this.pendingTag == null) {
            throw new IllegalStateException("Cannot write attribute when not in a start tag");
        }
        Triple t = new Triple();
        t.local = localName;
        t.value = value;
        this.pendingTag.attributes.add(t);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) {
        this.checkNonNull(prefix);
        this.checkNonNull(namespaceURI);
        this.checkNonNull(localName);
        this.checkNonNull(value);
        if (this.pendingTag == null) {
            throw new IllegalStateException("Cannot write attribute when not in a start tag");
        }
        Triple t = new Triple();
        t.prefix = prefix;
        t.uri = namespaceURI;
        t.local = localName;
        t.value = value;
        this.pendingTag.attributes.add(t);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) {
        this.checkNonNull(namespaceURI);
        this.checkNonNull(localName);
        this.checkNonNull(value);
        Triple t = new Triple();
        t.uri = namespaceURI;
        t.local = localName;
        t.value = value;
        this.pendingTag.attributes.add(t);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if (prefix == null || prefix.equals("") || prefix.equals("xmlns")) {
            this.writeDefaultNamespace(namespaceURI);
        } else {
            this.checkNonNull(namespaceURI);
            if (this.pendingTag == null) {
                throw new IllegalStateException("Cannot write namespace when not in a start tag");
            }
            Triple t = new Triple();
            t.uri = namespaceURI;
            t.prefix = prefix;
            this.pendingTag.namespaces.add(t);
        }
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) {
        this.checkNonNull(namespaceURI);
        if (this.pendingTag == null) {
            throw new IllegalStateException("Cannot write namespace when not in a start tag");
        }
        Triple t = new Triple();
        t.uri = namespaceURI;
        this.pendingTag.namespaces.add(t);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        this.flushStartTag();
        if (data == null) {
            data = "";
        }
        try {
            if (!this.isValidChars(data)) {
                throw new IllegalArgumentException("Invalid XML character in comment: " + data);
            }
            if (this.isChecking && data.contains("--")) {
                throw new IllegalArgumentException("Comment contains '--'");
            }
            this.receiver.comment(data, Loc.NONE, 0);
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        this.writeProcessingInstruction(target, "");
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        this.checkNonNull(target);
        this.checkNonNull(data);
        this.flushStartTag();
        try {
            if (this.isChecking) {
                if (!this.isValidNCName(target) || "xml".equalsIgnoreCase(target)) {
                    throw new IllegalArgumentException("Invalid PITarget: " + target);
                }
                if (!this.isValidChars(data)) {
                    throw new IllegalArgumentException("Invalid character in PI data: " + data);
                }
            }
            this.receiver.processingInstruction(target, data, Loc.NONE, 0);
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        this.checkNonNull(data);
        this.flushStartTag();
        this.writeCharacters(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
    }

    @Override
    public void writeEntityRef(String name) {
        throw new UnsupportedOperationException("writeEntityRef");
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        this.writeStartDocument("utf-8", "1.0");
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        this.writeStartDocument("utf-8", version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        if (encoding == null) {
            encoding = "utf-8";
        }
        if (version == null) {
            version = "1.0";
        }
        if (this.depth != -1) {
            throw new IllegalStateException("writeStartDocument must be the first call");
        }
        try {
            this.receiver.open();
            this.receiver.startDocument(0);
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
        this.depth = 0;
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        this.checkNonNull(text);
        this.flushStartTag();
        if (!this.isValidChars(text)) {
            throw new IllegalArgumentException("illegal XML character: " + text);
        }
        try {
            this.receiver.characters(text, Loc.NONE, 0);
        } catch (XPathException err) {
            throw new XMLStreamException(err);
        }
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        this.checkNonNull(text);
        this.writeCharacters(new String(text, start, len));
    }

    @Override
    public String getPrefix(String uri) {
        for (int i = this.setPrefixes.size() - 1; i >= 0; --i) {
            List bindings = (List)this.setPrefixes.get(i);
            for (int j = bindings.size() - 1; j >= 0; --j) {
                NamespaceBinding binding = (NamespaceBinding)bindings.get(j);
                if (!binding.getURI().equals(uri)) continue;
                return binding.getPrefix();
            }
        }
        if (this.rootNamespaceContext != null) {
            return this.rootNamespaceContext.getPrefix(uri);
        }
        return null;
    }

    @Override
    public void setPrefix(String prefix, String uri) {
        this.checkNonNull(prefix);
        if (uri == null) {
            uri = "";
        }
        if (this.isInvalidURI(uri)) {
            throw new IllegalArgumentException("Invalid namespace URI: " + uri);
        }
        if (!"".equals(prefix) && !this.isValidNCName(prefix)) {
            throw new IllegalArgumentException("Invalid namespace prefix: " + prefix);
        }
        this.setPrefixes.peek().add(new NamespaceBinding(prefix, uri));
    }

    @Override
    public void setDefaultNamespace(String uri) {
        this.setPrefix("", uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) {
        if (this.depth > 0) {
            throw new IllegalStateException("setNamespaceContext may only be called at the start of the document");
        }
        this.rootNamespaceContext = context;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return new NamespaceContext(){
            final NamespaceContext rootNamespaceContext;
            final Map<String, String> bindings;
            {
                this.rootNamespaceContext = StreamWriterToReceiver.this.rootNamespaceContext;
                this.bindings = new HashMap<String, String>();
                for (List list : StreamWriterToReceiver.this.setPrefixes) {
                    for (NamespaceBinding binding : list) {
                        this.bindings.put(binding.getPrefix(), binding.getURI());
                    }
                }
            }

            @Override
            public String getNamespaceURI(String prefix) {
                String uri = this.bindings.get(prefix);
                if (uri != null) {
                    return uri;
                }
                return this.rootNamespaceContext.getNamespaceURI(prefix);
            }

            @Override
            public String getPrefix(String namespaceURI) {
                for (Map.Entry<String, String> entry : this.bindings.entrySet()) {
                    if (!entry.getValue().equals(namespaceURI)) continue;
                    return entry.getKey();
                }
                return this.rootNamespaceContext.getPrefix(namespaceURI);
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                ArrayList<String> prefixes = new ArrayList<String>();
                for (Map.Entry<String, String> entry : this.bindings.entrySet()) {
                    if (!entry.getValue().equals(namespaceURI)) continue;
                    prefixes.add(entry.getKey());
                }
                Iterator root = this.rootNamespaceContext.getPrefixes(namespaceURI);
                while (root.hasNext()) {
                    prefixes.add((String)root.next());
                }
                return prefixes.iterator();
            }
        };
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name.equals("javax.xml.stream.isRepairingNamespaces")) {
            return this.receiver instanceof NamespaceReducer;
        }
        throw new IllegalArgumentException(name);
    }

    private boolean isValidNCName(String name) {
        return !this.isChecking || NameChecker.isValidNCName(name);
    }

    private boolean isValidChars(String text) {
        return !this.isChecking || UTF16CharacterSet.firstInvalidChar(text, this.charChecker) == -1;
    }

    private boolean isInvalidURI(String uri) {
        return this.isChecking && !StandardURIChecker.getInstance().isValidURI(uri);
    }

    private void checkNonNull(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }

    private static class StartTag {
        public Triple elementName = new Triple();
        public List<Triple> attributes = new ArrayList<Triple>();
        public List<Triple> namespaces = new ArrayList<Triple>();
    }

    private static class Triple {
        public String prefix;
        public String uri;
        public String local;
        public String value;

        private Triple() {
        }
    }
}

