/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.event.Builder;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

public class BuildingStreamWriterImpl
extends StreamWriterToReceiver
implements BuildingStreamWriter {
    Builder builder;

    public BuildingStreamWriterImpl(Receiver receiver, Builder builder) {
        super(receiver);
        this.builder = builder;
        builder.open();
    }

    @Override
    public XdmNode getDocumentNode() throws SaxonApiException {
        try {
            this.builder.close();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        return new XdmNode(this.builder.getCurrentRoot());
    }
}

