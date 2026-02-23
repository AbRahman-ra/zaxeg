/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.OutputterEvent;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public class OutputterEventBuffer
extends Outputter {
    private List<OutputterEvent> buffer = new ArrayList<OutputterEvent>();

    public void setBuffer(List<OutputterEvent> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.buffer.add(new OutputterEvent.StartDocument(properties));
    }

    @Override
    public void endDocument() throws XPathException {
        this.buffer.add(new OutputterEvent.EndDocument());
    }

    @Override
    public void startElement(NodeName elemName, SchemaType typeCode, Location location, int properties) {
        this.buffer.add(new OutputterEvent.StartElement(elemName, typeCode, location, properties));
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.buffer.add(new OutputterEvent.StartElement(elemName, type, location, properties));
        for (AttributeInfo att : attributes) {
            this.buffer.add(new OutputterEvent.Attribute(att.getNodeName(), att.getType(), att.getValue(), att.getLocation(), att.getProperties()));
        }
        for (NamespaceBinding binding : namespaces) {
            this.buffer.add(new OutputterEvent.Namespace(binding.getPrefix(), binding.getURI(), properties));
        }
        this.buffer.add(new OutputterEvent.StartContent());
    }

    @Override
    public void attribute(NodeName name, SimpleType type, CharSequence value, Location location, int properties) {
        this.buffer.add(new OutputterEvent.Attribute(name, type, value.toString(), location, properties));
    }

    @Override
    public void namespace(String prefix, String uri, int properties) {
        this.buffer.add(new OutputterEvent.Namespace(prefix, uri, properties));
    }

    @Override
    public void startContent() {
        this.buffer.add(new OutputterEvent.StartContent());
    }

    @Override
    public void endElement() throws XPathException {
        this.buffer.add(new OutputterEvent.EndElement());
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) {
        this.buffer.add(new OutputterEvent.Text(chars, location, properties));
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) {
        this.buffer.add(new OutputterEvent.ProcessingInstruction(name, data, location, properties));
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) {
        this.buffer.add(new OutputterEvent.Comment(content, location, properties));
    }

    @Override
    public void append(Item item, Location location, int properties) {
        this.buffer.add(new OutputterEvent.Append(item, location, properties));
    }

    @Override
    public void close() {
    }

    public void replay(Outputter out) throws XPathException {
        for (OutputterEvent event : this.buffer) {
            event.replay(out);
        }
    }

    public boolean isEmpty() {
        return this.buffer.isEmpty();
    }

    public void reset() {
        this.buffer.clear();
    }
}

