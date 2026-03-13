/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.util.Stack;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.HTMLTagHashSet;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.CompressedWhitespace;
import net.sf.saxon.type.SchemaType;

public abstract class HTMLEmitter
extends XMLEmitter {
    private static final int REP_NATIVE = 0;
    private static final int REP_ENTITY = 1;
    private static final int REP_DECIMAL = 2;
    private static final int REP_HEX = 3;
    private int nonASCIIRepresentation = 0;
    private int excludedRepresentation = 1;
    private int inScript;
    protected int version = 5;
    private String parentElement;
    private String uri;
    private boolean escapeNonAscii = false;
    private Stack<NodeName> nodeNameStack = new Stack();
    static HTMLTagHashSet emptyTags = new HTMLTagHashSet(31);
    private static HTMLTagHashSet booleanAttributes = new HTMLTagHashSet(43);
    private static HTMLTagHashSet booleanCombinations = new HTMLTagHashSet(57);

    private static int representationCode(String rep) {
        switch (rep = rep.toLowerCase()) {
            case "native": {
                return 0;
            }
            case "entity": {
                return 1;
            }
            case "decimal": {
                return 2;
            }
            case "hex": {
                return 3;
            }
        }
        return 1;
    }

    protected static void setEmptyTag(String tag) {
        emptyTags.add(tag);
    }

    protected static boolean isEmptyTag(String tag) {
        return emptyTags.contains(tag);
    }

    private static void setBooleanAttribute(String element, String attribute) {
        booleanAttributes.add(attribute);
        booleanCombinations.add(element + '+' + attribute);
    }

    private static boolean isBooleanAttribute(String element, String attribute, String value) {
        return attribute.equalsIgnoreCase(value) && booleanAttributes.contains(attribute) && (booleanCombinations.contains(element + '+' + attribute) || booleanCombinations.contains("*+" + attribute));
    }

    @Override
    public void setEscapeNonAscii(Boolean escape) {
        this.escapeNonAscii = escape;
    }

    protected abstract boolean isHTMLElement(NodeName var1);

    @Override
    public void open() throws XPathException {
    }

    @Override
    protected void openDocument() throws XPathException {
        if (this.writer == null) {
            this.makeWriter();
        }
        if (this.started) {
            return;
        }
        String byteOrderMark = this.outputProperties.getProperty("byte-order-mark");
        if ("yes".equals(byteOrderMark) && "UTF-8".equalsIgnoreCase(this.outputProperties.getProperty("encoding"))) {
            try {
                this.writer.write(65279);
            } catch (IOException iOException) {
                // empty catch block
            }
        }
        if ("yes".equals(this.outputProperties.getProperty("{http://saxon.sf.net/}single-quotes"))) {
            this.delimiter = (char)39;
            this.attSpecials = specialInAttSingle;
        }
        this.inScript = -1000000;
    }

    @Override
    protected void writeDocType(NodeName name, String displayName, String systemId, String publicId) throws XPathException {
        super.writeDocType(name, displayName, systemId, publicId);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.uri = elemName.getURI();
        super.startElement(elemName, type, attributes, namespaces, location, properties);
        this.parentElement = (String)this.elementStack.peek();
        if (this.isHTMLElement(elemName) && (this.parentElement.equalsIgnoreCase("script") || this.parentElement.equalsIgnoreCase("style"))) {
            this.inScript = 0;
        }
        ++this.inScript;
        this.nodeNameStack.push(elemName);
    }

    public void startContentOLD() throws XPathException {
        this.closeStartTag();
    }

    @Override
    protected void writeAttribute(NodeName elCode, String attname, CharSequence value, int properties) throws XPathException {
        try {
            if (this.isHTMLElement(elCode) && HTMLEmitter.isBooleanAttribute(elCode.getLocalPart(), attname, value.toString())) {
                this.writer.write(attname);
                return;
            }
            if (this.inScript > 0) {
                properties |= 1;
            }
            super.writeAttribute(elCode, attname, value, properties);
        } catch (IOException err) {
            throw new XPathException(err);
        }
    }

    @Override
    protected void writeEscape(CharSequence chars, boolean inAttribute) throws IOException, XPathException {
        boolean[] specialChars;
        int segstart = 0;
        boolean[] blArray = specialChars = inAttribute ? this.attSpecials : specialInText;
        if (chars instanceof CompressedWhitespace) {
            ((CompressedWhitespace)chars).writeEscape(specialChars, this.writer);
            return;
        }
        boolean disabled = false;
        while (segstart < chars.length()) {
            char c;
            int i;
            if (this.escapeNonAscii) {
                for (i = segstart; i < chars.length() && (c = chars.charAt(i)) < '\u007f' && !specialChars[c]; ++i) {
                }
            } else {
                while (i < chars.length() && ((c = chars.charAt(i)) < '\u007f' ? !specialChars[c] : this.characterSet.inCharset(c) && c > '\u00a0')) {
                    ++i;
                }
            }
            if (i == chars.length()) {
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
                this.writer.write(c);
            } else if (c <= '\u007f') {
                if (inAttribute) {
                    if (c == '<') {
                        this.writer.write(60);
                    } else if (c == '>') {
                        this.writer.write("&gt;");
                    } else if (c == '&') {
                        if (i + 1 < chars.length() && chars.charAt(i + 1) == '{') {
                            this.writer.write(38);
                        } else {
                            this.writer.write("&amp;");
                        }
                    } else if (c == '\"') {
                        this.writer.write("&#34;");
                    } else if (c == '\'') {
                        this.writer.write("&#39;");
                    } else if (c == '\n') {
                        this.writer.write("&#xA;");
                    } else if (c == '\t') {
                        this.writer.write("&#x9;");
                    } else if (c == '\r') {
                        this.writer.write("&#xD;");
                    }
                } else if (c == '<') {
                    this.writer.write("&lt;");
                } else if (c == '>') {
                    this.writer.write("&gt;");
                } else if (c == '&') {
                    this.writer.write("&amp;");
                } else if (c == '\r') {
                    this.writer.write("&#xD;");
                }
            } else if (c < '\u00a0') {
                if (this.rejectControlCharacters()) {
                    XPathException err = new XPathException("Illegal HTML character: decimal " + c);
                    err.setErrorCode("SERE0014");
                    throw err;
                }
                this.characterReferenceGenerator.outputCharacterReference(c, this.writer);
            } else if (c == '\u00a0') {
                this.writer.write("&nbsp;");
            } else if (c >= '\ud800' && c <= '\udbff') {
                int charval = (c - 55296) * 1024 + (chars.charAt(i + 1) - 56320) + 65536;
                this.characterReferenceGenerator.outputCharacterReference(charval, this.writer);
                ++i;
            } else if (this.escapeNonAscii || !this.characterSet.inCharset(c)) {
                this.characterReferenceGenerator.outputCharacterReference(c, this.writer);
            } else {
                this.writer.write(c);
            }
            segstart = ++i;
        }
    }

    protected abstract boolean rejectControlCharacters();

    @Override
    protected String emptyElementTagCloser(String displayName, NodeName nameCode) {
        if (this.isHTMLElement(nameCode)) {
            return "></" + displayName + ">";
        }
        return "/>";
    }

    @Override
    public void endElement() throws XPathException {
        NodeName nodeName = this.nodeNameStack.pop();
        String name = (String)this.elementStack.peek();
        --this.inScript;
        if (this.inScript == 0) {
            this.inScript = -1000000;
        }
        if (HTMLEmitter.isEmptyTag(name) && this.isHTMLElement(nodeName)) {
            if (this.openStartTag) {
                this.closeStartTag();
            }
            this.elementStack.pop();
        } else {
            super.endElement();
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.inScript > 0) {
            properties |= 1;
        }
        super.characters(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (!this.started) {
            this.openDocument();
        }
        for (int i = 0; i < data.length(); ++i) {
            if (data.charAt(i) != '>') continue;
            XPathException err = new XPathException("A processing instruction in HTML must not contain a > character");
            err.setErrorCode("SERE0015");
            throw err;
        }
        try {
            if (this.openStartTag) {
                this.closeStartTag();
            }
            this.writer.write("<?");
            this.writer.write(target);
            this.writer.write(32);
            this.writeCharSequence(data);
            this.writer.write(62);
        } catch (IOException err) {
            throw new XPathException(err);
        }
    }

    static {
        HTMLEmitter.setBooleanAttribute("*", "hidden");
        HTMLEmitter.setBooleanAttribute("area", "nohref");
        HTMLEmitter.setBooleanAttribute("audio", "autoplay");
        HTMLEmitter.setBooleanAttribute("audio", "controls");
        HTMLEmitter.setBooleanAttribute("audio", "loop");
        HTMLEmitter.setBooleanAttribute("audio", "muted");
        HTMLEmitter.setBooleanAttribute("button", "disabled");
        HTMLEmitter.setBooleanAttribute("button", "autofocus");
        HTMLEmitter.setBooleanAttribute("button", "formnovalidate");
        HTMLEmitter.setBooleanAttribute("details", "open");
        HTMLEmitter.setBooleanAttribute("dialog", "open");
        HTMLEmitter.setBooleanAttribute("dir", "compact");
        HTMLEmitter.setBooleanAttribute("dl", "compact");
        HTMLEmitter.setBooleanAttribute("fieldset", "disabled");
        HTMLEmitter.setBooleanAttribute("form", "novalidate");
        HTMLEmitter.setBooleanAttribute("frame", "noresize");
        HTMLEmitter.setBooleanAttribute("hr", "noshade");
        HTMLEmitter.setBooleanAttribute("img", "ismap");
        HTMLEmitter.setBooleanAttribute("input", "checked");
        HTMLEmitter.setBooleanAttribute("input", "disabled");
        HTMLEmitter.setBooleanAttribute("input", "multiple");
        HTMLEmitter.setBooleanAttribute("input", "readonly");
        HTMLEmitter.setBooleanAttribute("input", "required");
        HTMLEmitter.setBooleanAttribute("input", "autofocus");
        HTMLEmitter.setBooleanAttribute("input", "formnovalidate");
        HTMLEmitter.setBooleanAttribute("iframe", "seamless");
        HTMLEmitter.setBooleanAttribute("keygen", "autofocus");
        HTMLEmitter.setBooleanAttribute("keygen", "disabled");
        HTMLEmitter.setBooleanAttribute("menu", "compact");
        HTMLEmitter.setBooleanAttribute("object", "declare");
        HTMLEmitter.setBooleanAttribute("object", "typemustmatch");
        HTMLEmitter.setBooleanAttribute("ol", "compact");
        HTMLEmitter.setBooleanAttribute("ol", "reversed");
        HTMLEmitter.setBooleanAttribute("optgroup", "disabled");
        HTMLEmitter.setBooleanAttribute("option", "selected");
        HTMLEmitter.setBooleanAttribute("option", "disabled");
        HTMLEmitter.setBooleanAttribute("script", "defer");
        HTMLEmitter.setBooleanAttribute("script", "async");
        HTMLEmitter.setBooleanAttribute("select", "multiple");
        HTMLEmitter.setBooleanAttribute("select", "disabled");
        HTMLEmitter.setBooleanAttribute("select", "autofocus");
        HTMLEmitter.setBooleanAttribute("select", "required");
        HTMLEmitter.setBooleanAttribute("style", "scoped");
        HTMLEmitter.setBooleanAttribute("td", "nowrap");
        HTMLEmitter.setBooleanAttribute("textarea", "disabled");
        HTMLEmitter.setBooleanAttribute("textarea", "readonly");
        HTMLEmitter.setBooleanAttribute("textarea", "autofocus");
        HTMLEmitter.setBooleanAttribute("textarea", "required");
        HTMLEmitter.setBooleanAttribute("th", "nowrap");
        HTMLEmitter.setBooleanAttribute("track", "default");
        HTMLEmitter.setBooleanAttribute("ul", "compact");
        HTMLEmitter.setBooleanAttribute("video", "autoplay");
        HTMLEmitter.setBooleanAttribute("video", "controls");
        HTMLEmitter.setBooleanAttribute("video", "loop");
        HTMLEmitter.setBooleanAttribute("video", "muted");
    }
}

