/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.TeeOutputter;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;

public class TeeDestination
extends AbstractDestination {
    private Destination dest0;
    private Destination dest1;

    public TeeDestination(Destination destination0, Destination destination1) {
        this.dest0 = destination0;
        this.dest1 = destination1;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) throws SaxonApiException {
        return new TeeOutputter(this.dest0.getReceiver(pipe, params), this.dest1.getReceiver(pipe, params));
    }

    @Override
    public void close() throws SaxonApiException {
        this.dest0.close();
        this.dest1.close();
    }
}

