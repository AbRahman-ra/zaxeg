/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Arrays;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class TreeReceiver
extends SequenceReceiver {
    private Receiver nextReceiver;
    private int level = 0;
    private boolean[] isDocumentLevel = new boolean[20];

    public TreeReceiver(Receiver nextInChain) {
        super(nextInChain.getPipelineConfiguration());
        this.nextReceiver = nextInChain;
        this.previousAtomic = false;
        this.setPipelineConfiguration(nextInChain.getPipelineConfiguration());
    }

    @Override
    public void setSystemId(String systemId) {
        if (systemId != null && !systemId.equals(this.systemId)) {
            this.systemId = systemId;
            if (this.nextReceiver != null) {
                this.nextReceiver.setSystemId(systemId);
            }
        }
    }

    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipe) {
        if (this.pipelineConfiguration != pipe) {
            this.pipelineConfiguration = pipe;
            if (this.nextReceiver != null) {
                this.nextReceiver.setPipelineConfiguration(pipe);
            }
        }
    }

    public Receiver getNextReceiver() {
        return this.nextReceiver;
    }

    @Override
    public void open() throws XPathException {
        if (this.nextReceiver == null) {
            throw new IllegalStateException("TreeReceiver.open(): no underlying receiver provided");
        }
        this.nextReceiver.open();
        this.previousAtomic = false;
    }

    @Override
    public void close() throws XPathException {
        if (this.nextReceiver != null) {
            this.nextReceiver.close();
        }
        this.previousAtomic = false;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        if (this.level == 0) {
            this.nextReceiver.startDocument(properties);
        }
        if (this.isDocumentLevel.length - 1 < this.level) {
            this.isDocumentLevel = Arrays.copyOf(this.isDocumentLevel, this.level * 2);
        }
        this.isDocumentLevel[this.level++] = true;
    }

    @Override
    public void endDocument() throws XPathException {
        --this.level;
        if (this.level == 0) {
            this.nextReceiver.endDocument();
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        this.previousAtomic = false;
        if (this.isDocumentLevel.length - 1 < this.level) {
            this.isDocumentLevel = Arrays.copyOf(this.isDocumentLevel, this.level * 2);
        }
        this.isDocumentLevel[this.level++] = false;
    }

    @Override
    public void endElement() throws XPathException {
        this.nextReceiver.endElement();
        this.previousAtomic = false;
        --this.level;
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (chars.length() > 0) {
            this.nextReceiver.characters(chars, locationId, properties);
        }
        this.previousAtomic = false;
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
        this.previousAtomic = false;
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.nextReceiver.comment(chars, locationId, properties);
        this.previousAtomic = false;
    }

    @Override
    public void setUnparsedEntity(String name, String uri, String publicId) throws XPathException {
        this.nextReceiver.setUnparsedEntity(name, uri, publicId);
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        this.decompose(item, locationId, copyNamespaces);
    }

    @Override
    public boolean usesTypeAnnotations() {
        return this.nextReceiver.usesTypeAnnotations();
    }
}

