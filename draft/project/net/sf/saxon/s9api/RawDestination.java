/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.event.CloseNotifier;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;

public class RawDestination
extends AbstractDestination {
    private SequenceCollector sequenceOutputter;
    private boolean closed = false;

    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) {
        this.sequenceOutputter = new SequenceCollector(pipe);
        this.closed = false;
        this.helper.onClose(() -> {
            this.closed = true;
        });
        return new CloseNotifier(this.sequenceOutputter, this.helper.getListeners());
    }

    @Override
    public void close() throws SaxonApiException {
        try {
            this.sequenceOutputter.close();
            this.closed = true;
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
    }

    public XdmValue getXdmValue() {
        if (!this.closed) {
            throw new IllegalStateException("The result sequence has not yet been closed");
        }
        return XdmValue.wrap(this.sequenceOutputter.getSequence());
    }
}

