/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.HTMLEmitter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class HTML50Emitter
extends HTMLEmitter {
    public HTML50Emitter() {
        this.version = 5;
    }

    @Override
    protected boolean isHTMLElement(NodeName name) {
        String uri = name.getURI();
        return uri.equals("") || uri.equals("http://www.w3.org/1999/xhtml");
    }

    @Override
    protected void openDocument() throws XPathException {
        this.version = 5;
        super.openDocument();
    }

    @Override
    protected void writeDocType(NodeName name, String displayName, String systemId, String publicId) throws XPathException {
        try {
            if (systemId == null && publicId == null) {
                this.writer.write("<!DOCTYPE HTML>");
            } else {
                super.writeDocType(name, displayName, systemId, publicId);
            }
        } catch (IOException err) {
            throw new XPathException(err);
        }
    }

    @Override
    protected boolean writeDocTypeWithNullSystemId() {
        return true;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (!this.started) {
            this.openDocument();
            String systemId = this.outputProperties.getProperty("doctype-system");
            String publicId = this.outputProperties.getProperty("doctype-public");
            if ("".equals(systemId)) {
                systemId = null;
            }
            if ("".equals(publicId)) {
                publicId = null;
            }
            this.writeDocType(null, "html", systemId, publicId);
            this.started = true;
        }
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    protected boolean rejectControlCharacters() {
        return false;
    }

    static {
        HTML50Emitter.setEmptyTag("area");
        HTML50Emitter.setEmptyTag("base");
        HTML50Emitter.setEmptyTag("base");
        HTML50Emitter.setEmptyTag("basefont");
        HTML50Emitter.setEmptyTag("br");
        HTML50Emitter.setEmptyTag("col");
        HTML50Emitter.setEmptyTag("embed");
        HTML50Emitter.setEmptyTag("frame");
        HTML50Emitter.setEmptyTag("hr");
        HTML50Emitter.setEmptyTag("img");
        HTML50Emitter.setEmptyTag("input");
        HTML50Emitter.setEmptyTag("isindex");
        HTML50Emitter.setEmptyTag("keygen");
        HTML50Emitter.setEmptyTag("link");
        HTML50Emitter.setEmptyTag("meta");
        HTML50Emitter.setEmptyTag("param");
        HTML50Emitter.setEmptyTag("source");
        HTML50Emitter.setEmptyTag("track");
        HTML50Emitter.setEmptyTag("wbr");
    }
}

