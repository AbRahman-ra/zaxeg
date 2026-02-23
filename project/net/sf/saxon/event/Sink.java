/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class Sink
extends SequenceReceiver {
    public Sink(PipelineConfiguration pipe) {
        super(pipe);
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void startDocument(int properties) {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
    }

    @Override
    public void endElement() {
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) {
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) {
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) {
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) {
    }

    @Override
    public void setUnparsedEntity(String name, String uri, String publicId) {
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }
}

