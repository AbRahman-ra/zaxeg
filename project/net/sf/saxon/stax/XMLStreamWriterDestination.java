/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.stax;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.stax.ReceiverToXMLStreamWriter;

public class XMLStreamWriterDestination
extends AbstractDestination {
    private XMLStreamWriter writer;

    public XMLStreamWriterDestination(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public XMLStreamWriter getXMLStreamWriter() {
        return this.writer;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) throws SaxonApiException {
        ReceiverToXMLStreamWriter r = new ReceiverToXMLStreamWriter(this.writer);
        r.setPipelineConfiguration(pipe);
        return r;
    }

    @Override
    public void close() throws SaxonApiException {
        try {
            this.writer.close();
        } catch (XMLStreamException e) {
            throw new SaxonApiException(e);
        }
    }
}

