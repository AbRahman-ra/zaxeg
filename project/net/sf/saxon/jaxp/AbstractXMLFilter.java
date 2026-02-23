/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.io.IOException;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public abstract class AbstractXMLFilter
implements XMLFilter {
    XMLReader parser;
    ContentHandler contentHandler;
    LexicalHandler lexicalHandler;

    @Override
    public void setParent(XMLReader parent) {
        this.parser = parent;
    }

    @Override
    public XMLReader getParent() {
        return this.parser;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.parser.getFeature(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.parser.setFeature(name, value);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException {
        if (name.equals("http://xml.org/sax/properties/lexical-handler")) {
            return this.lexicalHandler;
        }
        throw new SAXNotRecognizedException(name);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/properties/lexical-handler")) {
            if (!(value instanceof LexicalHandler)) {
                throw new SAXNotSupportedException("Lexical Handler must be instance of org.xml.sax.ext.LexicalHandler");
            }
        } else {
            throw new SAXNotRecognizedException(name);
        }
        this.lexicalHandler = (LexicalHandler)value;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
        if (handler instanceof LexicalHandler && this.lexicalHandler == null) {
            this.lexicalHandler = (LexicalHandler)((Object)handler);
        }
    }

    @Override
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
    }

    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {
        InputSource input = new InputSource(systemId);
        this.parse(input);
    }
}

