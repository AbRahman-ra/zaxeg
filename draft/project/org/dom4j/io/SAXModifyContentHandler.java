/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.io;

import java.io.IOException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.io.DispatchHandler;
import org.dom4j.io.ElementStack;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXModifyElementHandler;
import org.dom4j.io.SAXModifyException;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class SAXModifyContentHandler
extends SAXContentHandler {
    private XMLWriter xmlWriter;

    public SAXModifyContentHandler() {
    }

    public SAXModifyContentHandler(DocumentFactory documentFactory) {
        super(documentFactory);
    }

    public SAXModifyContentHandler(DocumentFactory documentFactory, ElementHandler elementHandler) {
        super(documentFactory, elementHandler);
    }

    public SAXModifyContentHandler(DocumentFactory documentFactory, ElementHandler elementHandler, ElementStack elementStack) {
        super(documentFactory, elementHandler, elementStack);
    }

    public void setXMLWriter(XMLWriter writer) {
        this.xmlWriter = writer;
    }

    public void startCDATA() throws SAXException {
        super.startCDATA();
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.startCDATA();
        }
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        super.startDTD(name, publicId, systemId);
        if (this.xmlWriter != null) {
            this.xmlWriter.startDTD(name, publicId, systemId);
        }
    }

    public void endDTD() throws SAXException {
        super.endDTD();
        if (this.xmlWriter != null) {
            this.xmlWriter.endDTD();
        }
    }

    public void comment(char[] characters, int parm2, int parm3) throws SAXException {
        super.comment(characters, parm2, parm3);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.comment(characters, parm2, parm3);
        }
    }

    public void startEntity(String name) throws SAXException {
        super.startEntity(name);
        if (this.xmlWriter != null) {
            this.xmlWriter.startEntity(name);
        }
    }

    public void endCDATA() throws SAXException {
        super.endCDATA();
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.endCDATA();
        }
    }

    public void endEntity(String name) throws SAXException {
        super.endEntity(name);
        if (this.xmlWriter != null) {
            this.xmlWriter.endEntity(name);
        }
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notation) throws SAXException {
        super.unparsedEntityDecl(name, publicId, systemId, notation);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.unparsedEntityDecl(name, publicId, systemId, notation);
        }
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        super.notationDecl(name, publicId, systemId);
        if (this.xmlWriter != null) {
            this.xmlWriter.notationDecl(name, publicId, systemId);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qName, atts);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.startElement(uri, localName, qName, atts);
        }
    }

    public void startDocument() throws SAXException {
        super.startDocument();
        if (this.xmlWriter != null) {
            this.xmlWriter.startDocument();
        }
    }

    public void ignorableWhitespace(char[] parm1, int parm2, int parm3) throws SAXException {
        super.ignorableWhitespace(parm1, parm2, parm3);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.ignorableWhitespace(parm1, parm2, parm3);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        super.processingInstruction(target, data);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.processingInstruction(target, data);
        }
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        if (this.xmlWriter != null) {
            this.xmlWriter.setDocumentLocator(locator);
        }
    }

    public void skippedEntity(String name) throws SAXException {
        super.skippedEntity(name);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.skippedEntity(name);
        }
    }

    public void endDocument() throws SAXException {
        super.endDocument();
        if (this.xmlWriter != null) {
            this.xmlWriter.endDocument();
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        if (this.xmlWriter != null) {
            this.xmlWriter.startPrefixMapping(prefix, uri);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        ElementHandler currentHandler = this.getElementStack().getDispatchHandler().getHandler(this.getElementStack().getPath());
        super.endElement(uri, localName, qName);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            if (currentHandler == null) {
                this.xmlWriter.endElement(uri, localName, qName);
            } else if (currentHandler instanceof SAXModifyElementHandler) {
                SAXModifyElementHandler modifyHandler = (SAXModifyElementHandler)currentHandler;
                Element modifiedElement = modifyHandler.getModifiedElement();
                try {
                    this.xmlWriter.write(modifiedElement);
                } catch (IOException ex) {
                    throw new SAXModifyException(ex);
                }
            }
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        super.endPrefixMapping(prefix);
        if (this.xmlWriter != null) {
            this.xmlWriter.endPrefixMapping(prefix);
        }
    }

    public void characters(char[] parm1, int parm2, int parm3) throws SAXException {
        super.characters(parm1, parm2, parm3);
        if (!this.activeHandlers() && this.xmlWriter != null) {
            this.xmlWriter.characters(parm1, parm2, parm3);
        }
    }

    protected XMLWriter getXMLWriter() {
        return this.xmlWriter;
    }

    private boolean activeHandlers() {
        DispatchHandler handler = this.getElementStack().getDispatchHandler();
        return handler.getActiveHandlerCount() > 0;
    }
}

