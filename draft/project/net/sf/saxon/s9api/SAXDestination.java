/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.event.ContentHandlerProxy;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.serialize.SerializationProperties;
import org.xml.sax.ContentHandler;

public class SAXDestination
extends AbstractDestination {
    private ContentHandler contentHandler;

    public SAXDestination(ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) {
        ContentHandlerProxy chp = new ContentHandlerProxy();
        chp.setUnderlyingContentHandler(this.contentHandler);
        chp.setPipelineConfiguration(pipe);
        return params.makeSequenceNormalizer(chp);
    }

    @Override
    public void close() {
    }
}

