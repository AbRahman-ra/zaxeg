/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.value.StringValue;

public class CommentImpl
extends NodeImpl {
    String comment;
    String systemId;
    int lineNumber = -1;
    int columnNumber = -1;

    public CommentImpl(String content) {
        this.comment = content;
    }

    @Override
    public final String getStringValue() {
        return this.comment;
    }

    @Override
    public AtomicSequence atomize() {
        return new StringValue(this.getStringValue());
    }

    @Override
    public final int getNodeKind() {
        return 8;
    }

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.comment(this.comment, locationId, 0);
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        this.comment = stringValue.toString();
    }

    public void setLocation(String uri, int lineNumber, int columnNumber) {
        this.systemId = uri;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    @Override
    public String getSystemId() {
        return this.systemId;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return this.columnNumber;
    }
}

