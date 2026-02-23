/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;

public class DestinationHelper {
    private Destination helpee;
    private List<Action> listeners = new ArrayList<Action>();

    public DestinationHelper(Destination helpee) {
        this.helpee = helpee;
    }

    public final void onClose(Action listener) {
        this.listeners.add(listener);
    }

    public void closeAndNotify() throws SaxonApiException {
        this.helpee.close();
        for (Action action : this.listeners) {
            action.act();
        }
    }

    public List<Action> getListeners() {
        return this.listeners;
    }
}

