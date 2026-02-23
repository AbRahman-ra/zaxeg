/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.event.Event;
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

public class EventBuffer
extends SequenceReceiver {
    private final List<Event> buffer = new ArrayList<Event>();

    public EventBuffer(PipelineConfiguration pipe) {
        super(pipe);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.buffer.add(new Event.StartDocument(properties));
    }

    @Override
    public void endDocument() throws XPathException {
        this.buffer.add(new Event.EndDocument());
    }

    @Override
    public void startElement(NodeName elemName, SchemaType typeCode, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.buffer.add(new Event.StartElement(elemName, typeCode, attributes, namespaces, location, properties));
    }

    @Override
    public void endElement() throws XPathException {
        this.buffer.add(new Event.EndElement());
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) throws XPathException {
        this.buffer.add(new Event.Text(chars, location, properties));
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        this.buffer.add(new Event.ProcessingInstruction(name, data, location, properties));
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        this.buffer.add(new Event.Comment(content, location, properties));
    }

    @Override
    public void append(Item item, Location location, int properties) throws XPathException {
        this.buffer.add(new Event.Append(item, location, properties));
    }

    @Override
    public void close() throws XPathException {
    }

    public void replay(Receiver out) throws XPathException {
        for (Event event : this.buffer) {
            event.replay(out);
        }
    }
}

