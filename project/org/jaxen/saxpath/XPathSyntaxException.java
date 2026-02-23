/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath;

import org.jaxen.saxpath.SAXPathException;

public class XPathSyntaxException
extends SAXPathException {
    private static final long serialVersionUID = 3567675610742422397L;
    private String xpath;
    private int position;
    private static final String lineSeparator = System.getProperty("line.separator");

    public XPathSyntaxException(String xpath, int position, String message) {
        super(message);
        this.position = position;
        this.xpath = xpath;
    }

    public int getPosition() {
        return this.position;
    }

    public String getXPath() {
        return this.xpath;
    }

    public String toString() {
        return this.getClass() + ": " + this.getXPath() + ": " + this.getPosition() + ": " + this.getMessage();
    }

    private String getPositionMarker() {
        int pos = this.getPosition();
        StringBuffer buf = new StringBuffer(pos + 1);
        for (int i = 0; i < pos; ++i) {
            buf.append(" ");
        }
        buf.append("^");
        return buf.toString();
    }

    public String getMultilineMessage() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getMessage());
        buf.append(lineSeparator);
        buf.append(this.getXPath());
        buf.append(lineSeparator);
        buf.append(this.getPositionMarker());
        return buf.toString();
    }
}

