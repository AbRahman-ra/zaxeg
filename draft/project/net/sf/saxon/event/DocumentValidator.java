/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class DocumentValidator
extends ProxyReceiver {
    private boolean foundElement = false;
    private int level = 0;
    private String errorCode;

    public DocumentValidator(Receiver next, String errorCode) {
        super(next);
        this.errorCode = errorCode;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration config) {
        super.setPipelineConfiguration(config);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (this.foundElement && this.level == 0) {
            throw new XPathException("A valid document must have only one child element", this.errorCode);
        }
        this.foundElement = true;
        ++this.level;
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.level == 0) {
            if (Whitespace.isWhite(chars)) {
                return;
            }
            throw new XPathException("A valid document must contain no text outside the outermost element (found \"" + Err.truncate30(chars) + "\")", this.errorCode);
        }
        this.nextReceiver.characters(chars, locationId, properties);
    }

    @Override
    public void endElement() throws XPathException {
        --this.level;
        this.nextReceiver.endElement();
    }

    @Override
    public void endDocument() throws XPathException {
        if (this.level == 0) {
            if (!this.foundElement) {
                throw new XPathException("A valid document must have a child element", this.errorCode);
            }
            this.foundElement = false;
            this.nextReceiver.endDocument();
            this.level = -1;
        }
    }
}

