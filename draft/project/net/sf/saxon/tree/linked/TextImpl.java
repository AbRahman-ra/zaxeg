/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;

public class TextImpl
extends NodeImpl {
    private String content;

    public TextImpl(String content) {
        this.content = content;
    }

    public void appendStringValue(String content) {
        this.content = this.content + content;
    }

    @Override
    public String getStringValue() {
        return this.content;
    }

    @Override
    public final int getNodeKind() {
        return 3;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.characters(this.content, locationId, 0);
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        if (stringValue.length() == 0) {
            this.delete();
        } else {
            this.content = stringValue.toString();
        }
    }
}

