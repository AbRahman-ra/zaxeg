/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;

public interface Destination {
    public void setDestinationBaseURI(URI var1);

    public URI getDestinationBaseURI();

    public Receiver getReceiver(PipelineConfiguration var1, SerializationProperties var2) throws SaxonApiException;

    public void onClose(Action var1);

    public void closeAndNotify() throws SaxonApiException;

    public void close() throws SaxonApiException;
}

