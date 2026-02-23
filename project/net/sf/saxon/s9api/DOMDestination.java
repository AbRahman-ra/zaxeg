/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.dom.DOMWriter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;
import org.w3c.dom.Node;

public class DOMDestination
extends AbstractDestination {
    private DOMWriter domWriter = new DOMWriter();

    public DOMDestination(Node root) {
        this.domWriter.setNode(root);
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) {
        this.domWriter.setPipelineConfiguration(pipe);
        return params.makeSequenceNormalizer(this.domWriter);
    }

    @Override
    public void close() throws SaxonApiException {
    }
}

