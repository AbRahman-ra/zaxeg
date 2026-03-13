/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class XHTML5Emitter
extends XMLEmitter {
    private static String[] html5ElementNames = new String[]{"a", "abbr", "address", "area", "article", "aside", "audio", "b", "base", "bdi", "bdo", "blockquote", "body", "br", "button", "canvas", "caption", "cite", "code", "col", "colgroup", "datalist", "dd", "del", "details", "dfn", "dialog", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure", "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "keygen", "label", "legend", "li", "link", "map", "mark", "menu", "meta", "meter", "nav", "noscript", "object", "ol", "optgroup", "option", "output", "p", "param", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select", "small", "source", "span", "strong", "style", "sub", "summary", "sup", "table", "tbody", "td", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "u", "ul", "var", "video", "wbr"};
    static Set<String> html5Elements = new HashSet<String>(128);
    static Set<String> emptyTags5 = new HashSet<String>(31);
    private static String[] emptyTagNames5 = new String[]{"area", "base", "br", "col", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"};

    private boolean isRecognizedHtmlElement(NodeName name) {
        return name.hasURI("http://www.w3.org/1999/xhtml") || name.hasURI("") && html5Elements.contains(name.getLocalPart().toLowerCase());
    }

    @Override
    protected void writeDocType(NodeName name, String displayName, String systemId, String publicId) throws XPathException {
        if (systemId == null && this.isRecognizedHtmlElement(name) && name.getLocalPart().toLowerCase().equals("html")) {
            try {
                this.writer.write("<!DOCTYPE " + displayName + ">");
            } catch (IOException e) {
                throw new XPathException(e);
            }
        } else if (systemId != null) {
            super.writeDocType(name, displayName, systemId, publicId);
        }
    }

    @Override
    protected boolean writeDocTypeWithNullSystemId() {
        return true;
    }

    @Override
    protected String emptyElementTagCloser(String displayName, NodeName name) {
        if (this.isRecognizedHtmlElement(name) && emptyTags5.contains(name.getLocalPart())) {
            return "/>";
        }
        return "></" + displayName + '>';
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.started || !Whitespace.isWhite(chars)) {
            super.characters(chars, locationId, properties);
        }
    }

    static {
        Collections.addAll(emptyTags5, emptyTagNames5);
        Collections.addAll(html5Elements, html5ElementNames);
    }
}

