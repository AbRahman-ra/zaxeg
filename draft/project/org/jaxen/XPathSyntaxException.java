/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import org.jaxen.JaxenException;

public class XPathSyntaxException
extends JaxenException {
    private static final long serialVersionUID = 1980601567207604059L;
    private String xpath;
    private int position;

    public XPathSyntaxException(org.jaxen.saxpath.XPathSyntaxException e) {
        super(e);
        this.xpath = e.getXPath();
        this.position = e.getPosition();
    }

    public XPathSyntaxException(String xpath, int position, String message) {
        super(message);
        this.xpath = xpath;
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }

    public String getXPath() {
        return this.xpath;
    }

    public String getPositionMarker() {
        StringBuffer buf = new StringBuffer();
        int pos = this.getPosition();
        for (int i = 0; i < pos; ++i) {
            buf.append(" ");
        }
        buf.append("^");
        return buf.toString();
    }

    public String getMultilineMessage() {
        StringBuffer buf = new StringBuffer(this.getMessage());
        buf.append("\n");
        buf.append(this.getXPath());
        buf.append("\n");
        buf.append(this.getPositionMarker());
        return buf.toString();
    }
}

