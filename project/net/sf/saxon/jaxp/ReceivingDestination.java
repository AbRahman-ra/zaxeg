/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverWithOutputProperties;
import net.sf.saxon.event.SequenceNormalizer;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.serialize.SerializationProperties;

public class ReceivingDestination
extends AbstractDestination {
    private Receiver outputTarget;

    public ReceivingDestination(Receiver target) {
        this.outputTarget = target;
    }

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties properties) {
        if (this.acceptsRawOutput()) {
            return this.outputTarget;
        }
        return properties.makeSequenceNormalizer(this.outputTarget);
    }

    public boolean acceptsRawOutput() {
        if (this.outputTarget instanceof SequenceNormalizer) {
            return true;
        }
        if (this.outputTarget instanceof ReceiverWithOutputProperties) {
            return "no".equals(((ReceiverWithOutputProperties)this.outputTarget).getOutputProperties().getProperty("{http://saxon.sf.net/}require-well-formed"));
        }
        return false;
    }

    @Override
    public void close() throws SaxonApiException {
    }
}

