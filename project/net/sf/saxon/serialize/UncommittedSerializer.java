/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Properties;
import javax.xml.transform.Result;
import net.sf.saxon.event.EventBuffer;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class UncommittedSerializer
extends ProxyReceiver {
    private boolean committed = false;
    private EventBuffer pending = null;
    private Result finalResult;
    private SerializationProperties properties;

    public UncommittedSerializer(Result finalResult, Receiver next, SerializationProperties params) {
        super(next);
        this.finalResult = finalResult;
        this.properties = params;
    }

    @Override
    public void open() throws XPathException {
        this.committed = false;
    }

    @Override
    public void close() throws XPathException {
        if (!this.committed) {
            this.switchToMethod("xml");
        }
        this.getNextReceiver().close();
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.committed) {
            this.getNextReceiver().characters(chars, locationId, properties);
        } else {
            if (this.pending == null) {
                this.pending = new EventBuffer(this.getPipelineConfiguration());
            }
            this.pending.characters(chars, locationId, properties);
            if (!Whitespace.isWhite(chars)) {
                this.switchToMethod("xml");
            }
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (this.committed) {
            this.getNextReceiver().processingInstruction(target, data, locationId, properties);
        } else {
            if (this.pending == null) {
                this.pending = new EventBuffer(this.getPipelineConfiguration());
            }
            this.pending.processingInstruction(target, data, locationId, properties);
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.committed) {
            this.getNextReceiver().comment(chars, locationId, properties);
        } else {
            if (this.pending == null) {
                this.pending = new EventBuffer(this.getPipelineConfiguration());
            }
            this.pending.comment(chars, locationId, properties);
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (!this.committed) {
            String name = elemName.getLocalPart();
            String uri = elemName.getURI();
            if (name.equalsIgnoreCase("html") && uri.isEmpty()) {
                this.switchToMethod("html");
            } else if (name.equals("html") && uri.equals("http://www.w3.org/1999/xhtml")) {
                String version = this.properties.getProperties().getProperty("{http://saxon.sf.net/}stylesheet-version");
                if ("10".equals(version)) {
                    this.switchToMethod("xml");
                } else {
                    this.switchToMethod("xhtml");
                }
            } else {
                this.switchToMethod("xml");
            }
        }
        this.getNextReceiver().startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.committed) {
            this.getNextReceiver().startDocument(properties);
        } else {
            if (this.pending == null) {
                this.pending = new EventBuffer(this.getPipelineConfiguration());
            }
            this.pending.startDocument(properties);
        }
    }

    @Override
    public void endDocument() throws XPathException {
        if (!this.committed) {
            this.switchToMethod("xml");
        }
        this.getNextReceiver().endDocument();
    }

    private void switchToMethod(String method) throws XPathException {
        Properties newProperties = new Properties(this.properties.getProperties());
        newProperties.setProperty("method", method);
        SerializerFactory sf = this.getConfiguration().getSerializerFactory();
        SerializationProperties newParams = new SerializationProperties(newProperties, this.properties.getCharacterMapIndex());
        newParams.setValidationFactory(this.properties.getValidationFactory());
        Receiver target = sf.getReceiver(this.finalResult, newParams, this.getPipelineConfiguration());
        this.committed = true;
        target.open();
        if (this.pending != null) {
            this.pending.replay(target);
            this.pending = null;
        }
        this.setUnderlyingReceiver(target);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        if (item instanceof NodeInfo) {
            ((NodeInfo)item).copy(this, 2, locationId);
        } else {
            if (!this.committed) {
                this.switchToMethod("xml");
            }
            this.getNextReceiver().append(item);
        }
    }
}

