/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sink;
import net.sf.saxon.s9api.AbstractDestination;
import net.sf.saxon.serialize.SerializationProperties;

public class NullDestination
extends AbstractDestination {
    @Override
    public Receiver getReceiver(PipelineConfiguration pipe, SerializationProperties params) {
        return new Sink(pipe);
    }

    @Override
    public void close() {
    }
}

