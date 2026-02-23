/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.util.regex.Pattern;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class TEXTEmitter
extends XMLEmitter {
    private Pattern newlineMatcher = null;
    private String newlineRepresentation = null;

    @Override
    public void open() throws XPathException {
    }

    @Override
    protected void openDocument() throws XPathException {
        String encoding;
        if (this.writer == null) {
            this.makeWriter();
        }
        if (this.characterSet == null) {
            this.characterSet = UTF8CharacterSet.getInstance();
        }
        if ((encoding = this.outputProperties.getProperty("encoding")) == null || encoding.equalsIgnoreCase("utf8")) {
            encoding = "UTF-8";
        }
        String byteOrderMark = this.outputProperties.getProperty("byte-order-mark");
        String nl = this.outputProperties.getProperty("{http://saxon.sf.net/}newline");
        if (nl != null && !nl.equals("\n")) {
            this.newlineRepresentation = nl;
            this.newlineMatcher = Pattern.compile("\\n");
        }
        if ("yes".equals(byteOrderMark) && ("UTF-8".equalsIgnoreCase(encoding) || "UTF-16LE".equalsIgnoreCase(encoding) || "UTF-16BE".equalsIgnoreCase(encoding))) {
            try {
                this.writer.write(65279);
            } catch (IOException iOException) {
                // empty catch block
            }
        }
        this.started = true;
    }

    @Override
    public void writeDeclaration() {
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        int badchar;
        if (!this.started) {
            this.openDocument();
        }
        if (!ReceiverOption.contains(properties, 4) && (badchar = this.testCharacters(chars)) != 0) {
            throw new XPathException("Output character not available in this encoding (x" + Integer.toString(badchar, 16) + ")", "SERE0008");
        }
        if (this.newlineMatcher != null) {
            chars = this.newlineMatcher.matcher(chars).replaceAll(this.newlineRepresentation);
        }
        try {
            this.writer.write(chars.toString());
        } catch (IOException err) {
            throw new XPathException(err);
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.previousAtomic = false;
    }

    @Override
    public void endElement() {
    }

    @Override
    public void processingInstruction(String name, CharSequence value, Location locationId, int properties) throws XPathException {
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
    }
}

