/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Properties;
import java.util.Stack;
import javax.xml.transform.sax.TransformerHandler;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ContentHandlerProxyLocator;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.AttributeCollectionImpl;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class ContentHandlerProxy
implements Receiver {
    private PipelineConfiguration pipe;
    private String systemId;
    protected ContentHandler handler;
    protected LexicalHandler lexicalHandler;
    private int depth = 0;
    private boolean requireWellFormed = false;
    private boolean undeclareNamespaces = false;
    private final Stack<String> elementStack = new Stack();
    private final Stack<String> namespaceStack = new Stack();
    private ContentHandlerProxyTraceListener traceListener;
    private Location currentLocation = Loc.NONE;
    private static final String MARKER = "##";

    public void setUnderlyingContentHandler(ContentHandler handler) {
        this.handler = handler;
        if (handler instanceof LexicalHandler) {
            this.lexicalHandler = (LexicalHandler)((Object)handler);
        }
    }

    public ContentHandler getUnderlyingContentHandler() {
        return this.handler;
    }

    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    public Configuration getConfiguration() {
        return this.pipe.getConfiguration();
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    public ContentHandlerProxyTraceListener getTraceListener() {
        if (this.traceListener == null) {
            this.traceListener = new ContentHandlerProxyTraceListener();
        }
        return this.traceListener;
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        if (this.handler instanceof TransformerHandler) {
            try {
                ((TransformerHandler)this.handler).unparsedEntityDecl(name, publicID, systemID, "unknown");
            } catch (SAXException e) {
                throw new XPathException(e);
            }
        }
    }

    public void setOutputProperties(Properties details) {
        String prop = details.getProperty("{http://saxon.sf.net/}require-well-formed");
        if (prop != null) {
            this.requireWellFormed = prop.equals("yes");
        }
        if ((prop = details.getProperty("undeclare-prefixes")) != null) {
            this.undeclareNamespaces = prop.equals("yes");
        }
    }

    public boolean isRequireWellFormed() {
        return this.requireWellFormed;
    }

    public void setRequireWellFormed(boolean wellFormed) {
        this.requireWellFormed = wellFormed;
    }

    public boolean isUndeclareNamespaces() {
        return this.undeclareNamespaces;
    }

    public void setUndeclareNamespaces(boolean undeclareNamespaces) {
        this.undeclareNamespaces = undeclareNamespaces;
    }

    @Override
    public void open() throws XPathException {
        if (this.handler == null) {
            throw new IllegalStateException("ContentHandlerProxy.open(): no underlying handler provided");
        }
        try {
            ContentHandlerProxyLocator locator = new ContentHandlerProxyLocator(this);
            this.handler.setDocumentLocator(locator);
            this.handler.startDocument();
        } catch (SAXException err) {
            this.handleSAXException(err);
        }
        this.depth = 0;
    }

    @Override
    public void close() throws XPathException {
        if (this.depth >= 0) {
            try {
                this.handler.endDocument();
            } catch (SAXException err) {
                this.handleSAXException(err);
            }
        }
        this.depth = -1;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
    }

    @Override
    public void endDocument() throws XPathException {
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        Attributes atts2;
        ++this.depth;
        if (this.depth <= 0 && this.requireWellFormed) {
            this.notifyNotWellFormed();
        }
        this.currentLocation = location.saveLocation();
        this.namespaceStack.push(MARKER);
        for (NamespaceBinding ns : namespaces) {
            String prefix = ns.getPrefix();
            if (prefix.equals("xml")) {
                return;
            }
            String uri = ns.getURI();
            if (!this.undeclareNamespaces && uri.isEmpty() && !prefix.isEmpty()) {
                return;
            }
            try {
                this.handler.startPrefixMapping(prefix, uri);
                this.namespaceStack.push(prefix);
            } catch (SAXException err) {
                this.handleSAXException(err);
            }
        }
        if (attributes instanceof Attributes) {
            atts2 = (Attributes)((Object)attributes);
        } else {
            AttributeCollectionImpl aci = new AttributeCollectionImpl(this.getConfiguration(), attributes.size());
            for (AttributeInfo att : attributes) {
                aci.addAttribute(att.getNodeName(), BuiltInAtomicType.UNTYPED_ATOMIC, att.getValue(), att.getLocation(), att.getProperties());
            }
            atts2 = aci;
        }
        if (this.depth > 0 || !this.requireWellFormed) {
            try {
                String uri = elemName.getURI();
                String localName = elemName.getLocalPart();
                String qname = elemName.getDisplayName();
                this.handler.startElement(uri, localName, qname, atts2);
                this.elementStack.push(uri);
                this.elementStack.push(localName);
                this.elementStack.push(qname);
            } catch (SAXException e) {
                this.handleSAXException(e);
            }
        }
    }

    @Override
    public void endElement() throws XPathException {
        String prefix;
        if (this.depth > 0) {
            try {
                assert (!this.elementStack.isEmpty());
                String qname = this.elementStack.pop();
                String localName = this.elementStack.pop();
                String uri = this.elementStack.pop();
                this.handler.endElement(uri, localName, qname);
            } catch (SAXException err) {
                this.handleSAXException(err);
            }
        }
        while (!(prefix = this.namespaceStack.pop()).equals(MARKER)) {
            try {
                this.handler.endPrefixMapping(prefix);
            } catch (SAXException err) {
                this.handleSAXException(err);
            }
        }
        --this.depth;
        if (this.requireWellFormed && this.depth <= 0) {
            this.depth = Integer.MIN_VALUE;
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.currentLocation = locationId;
        boolean disable = ReceiverOption.contains(properties, 1);
        if (disable) {
            this.setEscaping(false);
        }
        try {
            if (this.depth <= 0 && this.requireWellFormed) {
                if (!Whitespace.isWhite(chars)) {
                    this.notifyNotWellFormed();
                }
            } else {
                this.handler.characters(chars.toString().toCharArray(), 0, chars.length());
            }
        } catch (SAXException err) {
            this.handleSAXException(err);
        }
        if (disable) {
            this.setEscaping(true);
        }
    }

    protected void notifyNotWellFormed() throws XPathException {
        XPathException err = new XPathException("The result tree cannot be supplied to the ContentHandler because it is not well-formed XML");
        err.setErrorCode("SXCH0002");
        throw err;
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.currentLocation = locationId;
        try {
            this.handler.processingInstruction(target, data.toString());
        } catch (SAXException err) {
            this.handleSAXException(err);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.currentLocation = locationId;
        try {
            if (this.lexicalHandler != null) {
                this.lexicalHandler.comment(chars.toString().toCharArray(), 0, chars.length());
            }
        } catch (SAXException err) {
            this.handleSAXException(err);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }

    private void setEscaping(boolean escaping) {
        try {
            this.handler.processingInstruction(escaping ? "javax.xml.transform.enable-output-escaping" : "javax.xml.transform.disable-output-escaping", "");
        } catch (SAXException err) {
            throw new AssertionError((Object)err);
        }
    }

    private void handleSAXException(SAXException err) throws XPathException {
        Exception nested = err.getException();
        if (nested instanceof XPathException) {
            throw (XPathException)nested;
        }
        if (nested instanceof SchemaException) {
            throw new XPathException(nested);
        }
        XPathException de = new XPathException(err);
        de.setErrorCode("SXCH0003");
        throw de;
    }

    public static class ContentHandlerProxyTraceListener
    implements TraceListener {
        private Stack<Item> contextItemStack;

        @Override
        public void setOutputDestination(Logger stream) {
        }

        public Stack getContextItemStack() {
            return this.contextItemStack;
        }

        @Override
        public void open(Controller controller) {
            this.contextItemStack = new Stack();
        }

        @Override
        public void close() {
            this.contextItemStack = null;
        }

        @Override
        public void startCurrentItem(Item currentItem) {
            if (this.contextItemStack == null) {
                this.contextItemStack = new Stack();
            }
            this.contextItemStack.push(currentItem);
        }

        @Override
        public void endCurrentItem(Item currentItem) {
            this.contextItemStack.pop();
        }
    }
}

