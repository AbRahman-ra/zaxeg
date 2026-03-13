/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.stax;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class ReceiverToXMLStreamWriter
implements Receiver {
    protected PipelineConfiguration pipe;
    protected Configuration config;
    protected String systemId;
    protected String baseURI;
    private XMLStreamWriter writer;

    public ReceiverToXMLStreamWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public XMLStreamWriter getXMLStreamWriter() {
        return this.writer;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        this.pipe = pipe;
        this.config = pipe.getConfiguration();
    }

    @Override
    public PipelineConfiguration getPipelineConfiguration() {
        return this.pipe;
    }

    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public void open() throws XPathException {
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        try {
            this.writer.writeStartDocument();
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void endDocument() throws XPathException {
        try {
            this.writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        String local = elemName.getLocalPart();
        String uri = elemName.getURI();
        String prefix = elemName.getPrefix();
        try {
            if (prefix.equals("") && uri.equals("")) {
                this.writer.writeStartElement(local);
            } else if (prefix.equals("")) {
                this.writer.writeStartElement(prefix, local, uri);
            } else {
                this.writer.writeStartElement(prefix, local, uri);
            }
            for (NamespaceBinding ns : namespaces) {
                this.writer.writeNamespace(ns.getPrefix(), ns.getURI());
            }
            for (AttributeInfo att : attributes) {
                NodeName attName = att.getNodeName();
                String attLocal = attName.getLocalPart();
                String attUri = attName.getURI();
                String attPrefix = attName.getPrefix();
                String value = att.getValue();
                if (attPrefix.equals("") && attUri.equals("")) {
                    this.writer.writeAttribute(attLocal, value);
                    continue;
                }
                if (attPrefix.equals("") & !attUri.equals("")) {
                    this.writer.writeAttribute(attUri, attLocal, value);
                    continue;
                }
                this.writer.writeAttribute(attPrefix, attUri, attLocal, value);
            }
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void endElement() throws XPathException {
        try {
            this.writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        try {
            this.writer.writeCharacters(chars.toString());
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location locationId, int properties) throws XPathException {
        try {
            this.writer.writeProcessingInstruction(name, data.toString());
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void comment(CharSequence content, Location locationId, int properties) throws XPathException {
        try {
            this.writer.writeComment(content.toString());
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public void close() throws XPathException {
        try {
            this.writer.close();
        } catch (XMLStreamException e) {
            throw new XPathException(e);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }
}

