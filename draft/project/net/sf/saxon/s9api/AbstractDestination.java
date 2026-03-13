/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.net.URI;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.DestinationHelper;
import net.sf.saxon.s9api.SaxonApiException;

public abstract class AbstractDestination
implements Destination {
    protected DestinationHelper helper = new DestinationHelper(this);
    private URI baseURI;

    @Override
    public void setDestinationBaseURI(URI baseURI) {
        this.baseURI = baseURI;
    }

    @Override
    public URI getDestinationBaseURI() {
        return this.baseURI;
    }

    @Override
    public final void onClose(Action listener) {
        this.helper.onClose(listener);
    }

    @Override
    public void closeAndNotify() throws SaxonApiException {
        this.helper.closeAndNotify();
    }
}

