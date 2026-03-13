/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.trans.XPathException;

public class QuitParsingException
extends XPathException {
    private boolean notifiedByConsumer = false;

    public QuitParsingException(boolean notifiedByConsumer) {
        super("No more input required", "SXQP0001");
        this.notifiedByConsumer = notifiedByConsumer;
    }

    public boolean isNotifiedByConsumer() {
        return this.notifiedByConsumer;
    }
}

