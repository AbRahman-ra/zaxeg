/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.value.StringValue;

public class ProcInstImpl
extends NodeImpl {
    String content;
    String name;
    String systemId;
    int lineNumber = -1;
    int columnNumber = -1;

    public ProcInstImpl(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public NodeName getNodeName() {
        return new NoNamespaceName(this.name);
    }

    @Override
    public String getStringValue() {
        return this.content;
    }

    @Override
    public AtomicSequence atomize() {
        return new StringValue(this.getStringValue());
    }

    @Override
    public final int getNodeKind() {
        return 7;
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

    @Override
    public void copy(Receiver out, int copyOptions, Location locationId) throws XPathException {
        out.processingInstruction(this.getLocalPart(), this.content, locationId, 0);
    }

    @Override
    public void rename(NodeName newNameCode) {
        this.name = newNameCode.getLocalPart();
    }

    @Override
    public void replaceStringValue(CharSequence stringValue) {
        this.content = stringValue.toString();
    }
}

