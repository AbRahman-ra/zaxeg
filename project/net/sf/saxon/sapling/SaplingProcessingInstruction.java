/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.sapling;

import java.util.Objects;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.sapling.SaplingNode;
import net.sf.saxon.trans.XPathException;

public class SaplingProcessingInstruction
extends SaplingNode {
    private String name;
    private String value;

    public SaplingProcessingInstruction(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getStringValue() {
        return this.value;
    }

    @Override
    public int getNodeKind() {
        return 7;
    }

    @Override
    protected void sendTo(Receiver receiver) throws XPathException {
        receiver.processingInstruction(this.name, this.value, Loc.NONE, 0);
    }
}

