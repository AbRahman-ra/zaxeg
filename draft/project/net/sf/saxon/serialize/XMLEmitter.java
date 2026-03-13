/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Stack;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.CharacterReferenceGenerator;
import net.sf.saxon.serialize.Emitter;
import net.sf.saxon.serialize.HexCharacterReferenceGenerator;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CharSlice;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class XMLEmitter
extends Emitter {
    protected boolean canonical = false;
    protected boolean started = false;
    protected boolean startedElement = false;
    protected boolean openStartTag = false;
    protected boolean declarationIsWritten = false;
    protected NodeName elementCode;
    protected int indentForNextAttribute = -1;
    protected boolean undeclareNamespaces = false;
    protected boolean unfailing = false;
    protected char delimiter = (char)34;
    protected boolean[] attSpecials = specialInAtt;
    protected Stack<String> elementStack = new Stack();
    private boolean indenting = false;
    private String indentChars = "\n                                                          ";
    private boolean requireWellFormed = false;
    protected CharacterReferenceGenerator characterReferenceGenerator = HexCharacterReferenceGenerator.THE_INSTANCE;
    static boolean[] specialInText;
    static boolean[] specialInAtt;
    static boolean[] specialInAttSingle;

    public void setCharacterReferenceGenerator(CharacterReferenceGenerator generator) {
        this.characterReferenceGenerator = generator;
    }

    public void setEscapeNonAscii(Boolean escape) {
    }

    @Override
    public void open() throws XPathException {
    }

    @Override
    public void startDocument(int properties) throws XPathException {
    }

    @Override
    public void endDocument() throws XPathException {
    }

    protected void openDocument() throws XPathException {
        if (this.writer == null) {
            this.makeWriter();
        }
        if (this.characterSet == null) {
            this.characterSet = UTF8CharacterSet.getInstance();
        }
        if (this.outputProperties == null) {
            this.outputProperties = new Properties();
        }
        this.undeclareNamespaces = "yes".equals(this.outputProperties.getProperty("undeclare-prefixes"));
        this.canonical = "yes".equals(this.outputProperties.getProperty("{http://saxon.sf.net/}canonical"));
        this.unfailing = "yes".equals(this.outputProperties.getProperty("{http://saxon.sf.net/}unfailing"));
        if ("yes".equals(this.outputProperties.getProperty("{http://saxon.sf.net/}single-quotes"))) {
            this.delimiter = (char)39;
            this.attSpecials = specialInAttSingle;
        }
        this.writeDeclaration();
    }

    public void writeDeclaration() throws XPathException {
        if (this.declarationIsWritten) {
            return;
        }
        this.declarationIsWritten = true;
        try {
            String systemId;
            String standalone;
            String version;
            String omitXMLDeclaration;
            this.indenting = "yes".equals(this.outputProperties.getProperty("indent"));
            String byteOrderMark = this.outputProperties.getProperty("byte-order-mark");
            String encoding = this.outputProperties.getProperty("encoding");
            if (encoding == null || encoding.equalsIgnoreCase("utf8") || this.canonical) {
                encoding = "UTF-8";
            }
            if ("yes".equals(byteOrderMark) && !this.canonical && ("UTF-8".equalsIgnoreCase(encoding) || "UTF-16LE".equalsIgnoreCase(encoding) || "UTF-16BE".equalsIgnoreCase(encoding))) {
                this.writer.write(65279);
            }
            if ((omitXMLDeclaration = this.outputProperties.getProperty("omit-xml-declaration")) == null) {
                omitXMLDeclaration = "no";
            }
            if (this.canonical) {
                omitXMLDeclaration = "yes";
            }
            if ((version = this.outputProperties.getProperty("version")) == null) {
                version = this.getConfiguration().getXMLVersion() == 10 ? "1.0" : "1.1";
            } else {
                if (!version.equals("1.0") && !version.equals("1.1")) {
                    if (this.unfailing) {
                        version = "1.0";
                    } else {
                        XPathException err = new XPathException("XML version must be 1.0 or 1.1");
                        err.setErrorCode("SESU0013");
                        throw err;
                    }
                }
                if (!version.equals("1.0") && omitXMLDeclaration.equals("yes") && this.outputProperties.getProperty("doctype-system") != null && !this.unfailing) {
                    XPathException err = new XPathException("Values of 'version', 'omit-xml-declaration', and 'doctype-system' conflict");
                    err.setErrorCode("SEPM0009");
                    throw err;
                }
            }
            String undeclare = this.outputProperties.getProperty("undeclare-prefixes");
            if ("yes".equals(undeclare)) {
                this.undeclareNamespaces = true;
            }
            if (version.equals("1.0") && this.undeclareNamespaces) {
                if (this.unfailing) {
                    this.undeclareNamespaces = false;
                } else {
                    XPathException err = new XPathException("Cannot undeclare namespaces with XML version 1.0");
                    err.setErrorCode("SEPM0010");
                    throw err;
                }
            }
            if ("omit".equals(standalone = this.outputProperties.getProperty("standalone"))) {
                standalone = null;
            }
            if (standalone != null) {
                this.requireWellFormed = true;
                if (omitXMLDeclaration.equals("yes") && !this.unfailing) {
                    XPathException err = new XPathException("Values of 'standalone' and 'omit-xml-declaration' conflict");
                    err.setErrorCode("SEPM0009");
                    throw err;
                }
            }
            if ((systemId = this.outputProperties.getProperty("doctype-system")) != null && !"".equals(systemId)) {
                this.requireWellFormed = true;
            }
            if (omitXMLDeclaration.equals("no")) {
                this.writer.write("<?xml version=\"" + version + "\" encoding=\"" + encoding + '\"' + (standalone != null ? " standalone=\"" + standalone + '\"' : "") + "?>");
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    protected void writeDocType(NodeName name, String displayName, String systemId, String publicId) throws XPathException {
        try {
            if (!this.canonical) {
                if (this.declarationIsWritten && !this.indenting) {
                    this.writer.write("\n");
                }
                this.writer.write("<!DOCTYPE " + displayName + '\n');
                String quotedSystemId = null;
                if (systemId != null) {
                    quotedSystemId = systemId.contains("\"") ? "'" + systemId + "'" : '\"' + systemId + '\"';
                }
                if (systemId != null && publicId == null) {
                    this.writer.write("  SYSTEM " + quotedSystemId + ">\n");
                } else if (systemId == null && publicId != null) {
                    this.writer.write("  PUBLIC \"" + publicId + "\">\n");
                } else {
                    this.writer.write("  PUBLIC \"" + publicId + "\" " + quotedSystemId + ">\n");
                }
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    @Override
    public void close() throws XPathException {
        if (!this.started) {
            this.openDocument();
        }
        try {
            if (this.writer != null) {
                this.writer.flush();
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
        super.close();
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        int badchar;
        this.previousAtomic = false;
        if (!this.started) {
            this.openDocument();
        } else if (this.requireWellFormed && this.elementStack.isEmpty() && this.startedElement && !this.unfailing) {
            XPathException err = new XPathException("When 'standalone' or 'doctype-system' is specified, the document must be well-formed; but this document contains more than one top-level element");
            err.setErrorCode("SEPM0004");
            throw err;
        }
        this.startedElement = true;
        String displayName = elemName.getDisplayName();
        if (!this.allCharactersEncodable && (badchar = this.testCharacters(displayName)) != 0) {
            XPathException err = new XPathException("Element name contains a character (decimal + " + badchar + ") not available in the selected encoding");
            err.setErrorCode("SERE0008");
            throw err;
        }
        this.elementStack.push(displayName);
        this.elementCode = elemName;
        try {
            if (!this.started) {
                String systemId = this.outputProperties.getProperty("doctype-system");
                String publicId = this.outputProperties.getProperty("doctype-public");
                if ("".equals(systemId)) {
                    systemId = null;
                }
                if ("".equals(publicId)) {
                    publicId = null;
                }
                if (systemId != null) {
                    this.requireWellFormed = true;
                    this.writeDocType(elemName, displayName, systemId, publicId);
                } else if (this.writeDocTypeWithNullSystemId()) {
                    this.writeDocType(elemName, displayName, null, publicId);
                }
                this.started = true;
            }
            if (this.openStartTag) {
                this.closeStartTag();
            }
            this.writer.write(60);
            this.writer.write(displayName);
            if (this.indentForNextAttribute >= 0) {
                this.indentForNextAttribute += displayName.length();
            }
            boolean isFirst = true;
            for (NamespaceBinding ns : namespaces) {
                this.namespace(ns.getPrefix(), ns.getURI(), isFirst);
                isFirst = false;
            }
            for (AttributeInfo att : attributes) {
                this.attribute(att.getNodeName(), att.getValue(), att.getProperties(), isFirst);
                isFirst = false;
            }
            this.openStartTag = true;
            this.indentForNextAttribute = -1;
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    protected boolean writeDocTypeWithNullSystemId() {
        return false;
    }

    public void namespace(String nsprefix, String nsuri, boolean isFirst) throws XPathException {
        try {
            String sep;
            String string = sep = isFirst ? " " : this.getAttributeIndentString();
            if (nsprefix.isEmpty()) {
                this.writer.write(sep);
                this.writeAttribute(this.elementCode, "xmlns", nsuri, 0);
            } else if (!nsprefix.equals("xml")) {
                int badchar = this.testCharacters(nsprefix);
                if (badchar != 0) {
                    XPathException err = new XPathException("Namespace prefix contains a character (decimal + " + badchar + ") not available in the selected encoding");
                    err.setErrorCode("SERE0008");
                    throw err;
                }
                if (this.undeclareNamespaces || !nsuri.isEmpty()) {
                    this.writer.write(sep);
                    this.writeAttribute(this.elementCode, "xmlns:" + nsprefix, nsuri, 0);
                }
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    public void setIndentForNextAttribute(int indent) {
        this.indentForNextAttribute = indent;
    }

    private void attribute(NodeName nameCode, CharSequence value, int properties, boolean isFirst) throws XPathException {
        int badchar;
        String displayName = nameCode.getDisplayName();
        if (!this.allCharactersEncodable && (badchar = this.testCharacters(displayName)) != 0) {
            if (this.unfailing) {
                displayName = this.convertToAscii(displayName);
            } else {
                XPathException err = new XPathException("Attribute name contains a character (decimal + " + badchar + ") not available in the selected encoding");
                err.setErrorCode("SERE0008");
                throw err;
            }
        }
        try {
            this.writer.write(isFirst ? " " : this.getAttributeIndentString());
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
        this.writeAttribute(this.elementCode, displayName, value, properties);
    }

    protected String getAttributeIndentString() {
        if (this.indentForNextAttribute < 0) {
            return " ";
        }
        int indent = this.indentForNextAttribute;
        while (indent >= this.indentChars.length()) {
            this.indentChars = this.indentChars + "                     ";
        }
        return this.indentChars.substring(0, indent);
    }

    public void closeStartTag() throws XPathException {
        try {
            if (this.openStartTag) {
                this.writer.write(62);
                this.openStartTag = false;
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    protected String emptyElementTagCloser(String displayName, NodeName nameCode) {
        return this.canonical ? "></" + displayName + ">" : "/>";
    }

    protected void writeAttribute(NodeName elCode, String attname, CharSequence value, int properties) throws XPathException {
        try {
            String val = value.toString();
            this.writer.write(attname);
            if (ReceiverOption.contains(properties, 4)) {
                this.writer.write(61);
                this.writer.write(this.delimiter);
                this.writer.write(val);
                this.writer.write(this.delimiter);
            } else if (ReceiverOption.contains(properties, 256)) {
                this.writer.write(61);
                int delim = val.indexOf(34) >= 0 && val.indexOf(39) < 0 ? 39 : this.delimiter;
                this.writer.write(delim);
                this.writeEscape(value, true);
                this.writer.write(delim);
            } else {
                this.writer.write("=");
                this.writer.write(this.delimiter);
                if (ReceiverOption.contains(properties, 1)) {
                    this.writer.write(value.toString());
                } else {
                    this.writeEscape(value, true);
                }
                this.writer.write(this.delimiter);
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    protected int testCharacters(CharSequence chars) {
        for (int i = 0; i < chars.length(); ++i) {
            char c = chars.charAt(i);
            if (c <= '\u007f') continue;
            if (UTF16CharacterSet.isHighSurrogate(c)) {
                int cc;
                if (this.characterSet.inCharset(cc = UTF16CharacterSet.combinePair(c, chars.charAt(++i)))) continue;
                return cc;
            }
            if (this.characterSet.inCharset(c)) continue;
            return c;
        }
        return 0;
    }

    protected String convertToAscii(CharSequence chars) {
        FastStringBuffer buff = new FastStringBuffer(chars.length());
        for (int i = 0; i < chars.length(); ++i) {
            char c = chars.charAt(i);
            if (c >= '\u0014' && c < '\u007f') {
                buff.cat(c);
                continue;
            }
            buff.append("_" + c + "_");
        }
        return buff.toString();
    }

    @Override
    public void endElement() throws XPathException {
        String displayName = this.elementStack.pop();
        try {
            if (this.openStartTag) {
                this.writer.write(this.emptyElementTagCloser(displayName, this.elementCode));
                this.openStartTag = false;
            } else {
                this.writer.write("</");
                this.writer.write(displayName);
                this.writer.write(62);
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (!this.started) {
            this.openDocument();
        }
        if (this.requireWellFormed && this.elementStack.isEmpty() && !Whitespace.isWhite(chars) && !this.unfailing) {
            XPathException err = new XPathException("When 'standalone' or 'doctype-system' is specified, the document must be well-formed; but this document contains a top-level text node");
            err.setErrorCode("SEPM0004");
            throw err;
        }
        try {
            if (this.openStartTag) {
                this.closeStartTag();
            }
            if (ReceiverOption.contains(properties, 4)) {
                this.writeCharSequence(chars);
            } else if (!ReceiverOption.contains(properties, 1)) {
                this.writeEscape(chars, false);
            } else if (this.testCharacters(chars) == 0) {
                if (!ReceiverOption.contains(properties, 256)) {
                    this.writeCharSequence(chars);
                } else {
                    int len = chars.length();
                    for (int i = 0; i < len; ++i) {
                        char c = chars.charAt(i);
                        if (c == '\u0000') continue;
                        this.writer.write(c);
                    }
                }
            } else {
                int len = chars.length();
                for (int i = 0; i < len; ++i) {
                    char c = chars.charAt(i);
                    if (c == '\u0000') continue;
                    if (c > '\u007f' && UTF16CharacterSet.isHighSurrogate(c)) {
                        char[] pair;
                        int cc;
                        if (!this.characterSet.inCharset(cc = UTF16CharacterSet.combinePair(c, (pair = new char[]{c, chars.charAt(++i)})[1]))) {
                            this.writeEscape(new CharSlice(pair), false);
                            continue;
                        }
                        this.writeCharSequence(new CharSlice(pair));
                        continue;
                    }
                    char[] ca = new char[]{c};
                    if (!this.characterSet.inCharset(c)) {
                        this.writeEscape(new CharSlice(ca), false);
                        continue;
                    }
                    this.writeCharSequence(new CharSlice(ca));
                }
            }
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    public void writeCharSequence(CharSequence s) throws IOException {
        if (s instanceof String) {
            this.writer.write((String)s);
        } else if (s instanceof CharSlice) {
            ((CharSlice)s).write(this.writer);
        } else if (s instanceof FastStringBuffer) {
            ((FastStringBuffer)s).write(this.writer);
        } else if (s instanceof CompressedWhitespace) {
            ((CompressedWhitespace)s).write(this.writer);
        } else {
            this.writer.write(s.toString());
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        int x;
        if (!this.started) {
            this.openDocument();
        }
        if ((x = this.testCharacters(target)) != 0) {
            if (this.unfailing) {
                target = this.convertToAscii(target);
            } else {
                XPathException err = new XPathException("Character in processing instruction name cannot be represented in the selected encoding (code " + x + ')');
                err.setErrorCode("SERE0008");
                throw err;
            }
        }
        if ((x = this.testCharacters(data)) != 0) {
            if (this.unfailing) {
                data = this.convertToAscii(data);
            } else {
                XPathException err = new XPathException("Character in processing instruction data cannot be represented in the selected encoding (code " + x + ')');
                err.setErrorCode("SERE0008");
                throw err;
            }
        }
        try {
            if (this.openStartTag) {
                this.closeStartTag();
            }
            this.writer.write("<?" + target + (data.length() > 0 ? ' ' + data.toString() : "") + "?>");
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    protected void writeEscape(CharSequence chars, boolean inAttribute) throws IOException, XPathException {
        boolean[] specialChars;
        int segstart = 0;
        boolean disabled = false;
        boolean[] blArray = specialChars = inAttribute ? this.attSpecials : specialInText;
        if (chars instanceof CompressedWhitespace) {
            ((CompressedWhitespace)chars).writeEscape(specialChars, this.writer);
            return;
        }
        int clength = chars.length();
        while (segstart < clength) {
            char c;
            int i = segstart;
            while (i < clength) {
                c = chars.charAt(i);
                if (c < '\u007f') {
                    if (specialChars[c]) break;
                    ++i;
                    continue;
                }
                if (c < '\u00a0' || c == '\u2028' || UTF16CharacterSet.isHighSurrogate(c) || !this.characterSet.inCharset(c)) break;
                ++i;
            }
            if (i >= clength) {
                if (segstart == 0) {
                    this.writeCharSequence(chars);
                } else {
                    this.writeCharSequence(chars.subSequence(segstart, i));
                }
                return;
            }
            if (i > segstart) {
                this.writeCharSequence(chars.subSequence(segstart, i));
            }
            if ((c = chars.charAt(i)) == '\u0000') {
                disabled = !disabled;
            } else if (disabled) {
                if (c > '\u007f') {
                    if (UTF16CharacterSet.isHighSurrogate(c)) {
                        int cc = UTF16CharacterSet.combinePair(c, chars.charAt(i + 1));
                        if (!this.characterSet.inCharset(cc)) {
                            XPathException de = new XPathException("Character x" + Integer.toHexString(cc) + " is not available in the chosen encoding");
                            de.setErrorCode("SERE0008");
                            throw de;
                        }
                    } else if (!this.characterSet.inCharset(c)) {
                        XPathException de = new XPathException("Character " + c + " (x" + Integer.toHexString(c) + ") is not available in the chosen encoding");
                        de.setErrorCode("SERE0008");
                        throw de;
                    }
                }
                this.writer.write(c);
            } else if (c < '\u007f') {
                switch (c) {
                    case '<': {
                        this.writer.write("&lt;");
                        break;
                    }
                    case '>': {
                        this.writer.write("&gt;");
                        break;
                    }
                    case '&': {
                        this.writer.write("&amp;");
                        break;
                    }
                    case '\"': {
                        this.writer.write("&#34;");
                        break;
                    }
                    case '\'': {
                        this.writer.write("&#39;");
                        break;
                    }
                    case '\n': {
                        this.writer.write("&#xA;");
                        break;
                    }
                    case '\r': {
                        this.writer.write("&#xD;");
                        break;
                    }
                    case '\t': {
                        this.writer.write("&#x9;");
                        break;
                    }
                    default: {
                        this.characterReferenceGenerator.outputCharacterReference(c, this.writer);
                        break;
                    }
                }
            } else if (c < '\u00a0' || c == '\u2028') {
                this.characterReferenceGenerator.outputCharacterReference(c, this.writer);
            } else if (UTF16CharacterSet.isHighSurrogate(c)) {
                char d;
                int charval;
                if (this.characterSet.inCharset(charval = UTF16CharacterSet.combinePair(c, d = chars.charAt(++i)))) {
                    this.writer.write(c);
                    this.writer.write(d);
                } else {
                    this.characterReferenceGenerator.outputCharacterReference(charval, this.writer);
                }
            } else {
                this.characterReferenceGenerator.outputCharacterReference(c, this.writer);
            }
            segstart = ++i;
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        int x;
        if (!this.started) {
            this.openDocument();
        }
        if ((x = this.testCharacters(chars)) != 0) {
            if (this.unfailing) {
                chars = this.convertToAscii(chars);
            } else {
                XPathException err = new XPathException("Character in comment cannot be represented in the selected encoding (code " + x + ')');
                err.setErrorCode("SERE0008");
                throw err;
            }
        }
        try {
            if (this.openStartTag) {
                this.closeStartTag();
            }
            this.writer.write("<!--");
            this.writer.write(chars.toString());
            this.writer.write("-->");
        } catch (IOException err) {
            throw new XPathException("Failure writing to " + this.getSystemId(), err);
        }
    }

    @Override
    public boolean usesTypeAnnotations() {
        return false;
    }

    public boolean isStarted() {
        return this.started;
    }

    static {
        int i;
        specialInText = new boolean[128];
        for (i = 0; i <= 31; ++i) {
            XMLEmitter.specialInText[i] = true;
        }
        for (i = 32; i <= 127; ++i) {
            XMLEmitter.specialInText[i] = false;
        }
        XMLEmitter.specialInText[10] = false;
        XMLEmitter.specialInText[9] = false;
        XMLEmitter.specialInText[13] = true;
        XMLEmitter.specialInText[60] = true;
        XMLEmitter.specialInText[62] = true;
        XMLEmitter.specialInText[38] = true;
        specialInAtt = new boolean[128];
        for (i = 0; i <= 31; ++i) {
            XMLEmitter.specialInAtt[i] = true;
        }
        for (i = 32; i <= 127; ++i) {
            XMLEmitter.specialInAtt[i] = false;
        }
        XMLEmitter.specialInAtt[0] = true;
        XMLEmitter.specialInAtt[13] = true;
        XMLEmitter.specialInAtt[10] = true;
        XMLEmitter.specialInAtt[9] = true;
        XMLEmitter.specialInAtt[60] = true;
        XMLEmitter.specialInAtt[62] = true;
        XMLEmitter.specialInAtt[38] = true;
        XMLEmitter.specialInAtt[34] = true;
        specialInAttSingle = Arrays.copyOf(specialInAtt, 128);
        XMLEmitter.specialInAttSingle[34] = false;
        XMLEmitter.specialInAttSingle[39] = true;
    }
}

