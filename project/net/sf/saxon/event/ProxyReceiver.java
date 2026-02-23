/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class ProxyReceiver
extends SequenceReceiver {
    protected Receiver nextReceiver;

    public ProxyReceiver(Receiver nextReceiver) {
        super(nextReceiver.getPipelineConfiguration());
        this.setUnderlyingReceiver(nextReceiver);
        this.setPipelineConfiguration(nextReceiver.getPipelineConfiguration());
    }

    @Override
    public void setSystemId(String systemId) {
        if (systemId != this.systemId) {
            this.systemId = systemId;
            this.nextReceiver.setSystemId(systemId);
        }
    }

    public void setUnderlyingReceiver(Receiver receiver) {
        this.nextReceiver = receiver;
    }

    public Receiver getNextReceiver() {
        return this.nextReceiver;
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        if (this.pipelineConfiguration != pipe) {
            this.pipelineConfiguration = pipe;
            if (this.nextReceiver.getPipelineConfiguration() != pipe) {
                this.nextReceiver.setPipelineConfiguration(pipe);
            }
        }
    }

    @Override
    public NamePool getNamePool() {
        return this.pipelineConfiguration.getConfiguration().getNamePool();
    }

    @Override
    public void open() throws XPathException {
        this.nextReceiver.open();
    }

    @Override
    public void close() throws XPathException {
        this.nextReceiver.close();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.nextReceiver.startDocument(properties);
    }

    @Override
    public void endDocument() throws XPathException {
        this.nextReceiver.endDocument();
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        this.nextReceiver.endElement();
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.nextReceiver.characters(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.nextReceiver.comment(chars, locationId, properties);
    }

    @Override
    public void setUnparsedEntity(String name, String uri, String publicId) throws XPathException {
        this.nextReceiver.setUnparsedEntity(name, uri, publicId);
    }

    @Override
    public void append(Item item, Location locationId, int properties) throws XPathException {
        this.nextReceiver.append(item, locationId, properties);
    }

    @Override
    public boolean usesTypeAnnotations() {
        return this.nextReceiver.usesTypeAnnotations();
    }
}

