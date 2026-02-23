/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.HTMLEmitter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class HTML40Emitter
extends HTMLEmitter {
    @Override
    protected boolean isHTMLElement(NodeName name) {
        return name.getURI().equals("");
    }

    @Override
    protected void openDocument() throws XPathException {
        String versionProperty = this.outputProperties.getProperty("html-version");
        if (versionProperty == null) {
            versionProperty = this.outputProperties.getProperty("version");
        }
        if (versionProperty != null) {
            if (versionProperty.equals("4.0") || versionProperty.equals("4.01")) {
                this.version = 4;
            } else {
                XPathException err = new XPathException("Unsupported HTML version: " + versionProperty);
                err.setErrorCode("SESU0013");
                throw err;
            }
        }
        super.openDocument();
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
            if (systemId != null || publicId != null) {
                this.writeDocType(null, "html", systemId, publicId);
            }
            this.started = true;
        }
        super.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    protected boolean rejectControlCharacters() {
        return true;
    }

    static {
        HTML40Emitter.setEmptyTag("area");
        HTML40Emitter.setEmptyTag("base");
        HTML40Emitter.setEmptyTag("basefont");
        HTML40Emitter.setEmptyTag("br");
        HTML40Emitter.setEmptyTag("col");
        HTML40Emitter.setEmptyTag("embed");
        HTML40Emitter.setEmptyTag("frame");
        HTML40Emitter.setEmptyTag("hr");
        HTML40Emitter.setEmptyTag("img");
        HTML40Emitter.setEmptyTag("input");
        HTML40Emitter.setEmptyTag("isindex");
        HTML40Emitter.setEmptyTag("link");
        HTML40Emitter.setEmptyTag("meta");
        HTML40Emitter.setEmptyTag("param");
    }
}

