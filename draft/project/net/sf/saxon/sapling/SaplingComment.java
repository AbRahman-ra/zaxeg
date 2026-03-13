/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sapling;

import java.util.Objects;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.sapling.SaplingNode;
import net.sf.saxon.trans.XPathException;

public class SaplingComment
extends SaplingNode {
    private String value;

    public SaplingComment(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    public int getNodeKind() {
        return 8;
    }

    public String getStringValue() {
        return this.value;
    }

    @Override
    protected void sendTo(Receiver receiver) throws XPathException {
        receiver.comment(this.value, Loc.NONE, 0);
    }
}

