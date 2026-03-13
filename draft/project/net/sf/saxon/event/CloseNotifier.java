/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.List;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

public class CloseNotifier
extends ProxyReceiver {
    private final List<Action> actionList;

    public CloseNotifier(Receiver next, List<Action> actionList) {
        super(next);
        this.actionList = actionList;
    }

    @Override
    public void close() throws XPathException {
        super.close();
        try {
            if (this.actionList != null) {
                for (Action action : this.actionList) {
                    action.act();
                }
            }
        } catch (SaxonApiException e) {
            throw XPathException.makeXPathException(e);
        }
    }
}

