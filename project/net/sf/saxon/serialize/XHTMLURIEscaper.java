/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.HashSet;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.HTMLURIEscaper;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class XHTMLURIEscaper
extends HTMLURIEscaper {
    private static HashSet<String> urlTable = new HashSet(70);
    private static HashSet<String> attTable = new HashSet(20);

    private static void setUrlAttribute(String element, String attribute) {
        attTable.add(attribute);
        urlTable.add(element + "+" + attribute);
    }

    public XHTMLURIEscaper(Receiver next) {
        super(next);
    }

    private static boolean isURLAttribute(NodeName elcode, NodeName atcode) {
        if (!elcode.hasURI("http://www.w3.org/1999/xhtml")) {
            return false;
        }
        if (!atcode.hasURI("")) {
            return false;
        }
        String attName = atcode.getLocalPart();
        return attTable.contains(attName) && urlTable.contains(elcode.getLocalPart() + "+" + attName);
    }

    @Override
    public void startElement(NodeName nameCode, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.currentElement = nameCode;
        AttributeMap atts2 = attributes;
        if (this.escapeURIAttributes) {
            try {
                atts2 = attributes.apply(att -> {
                    if (!ReceiverOption.contains(att.getProperties(), 1)) {
                        NodeName attName = att.getNodeName();
                        if (this.isUrlAttribute(nameCode, attName)) {
                            String value = att.getValue();
                            try {
                                String normalized = XHTMLURIEscaper.isAllAscii(value) ? value : Normalizer.make(2, this.getConfiguration()).normalize(value);
                                return new AttributeInfo(attName, att.getType(), XHTMLURIEscaper.escapeURL(normalized, true, this.getConfiguration()).toString(), att.getLocation(), att.getProperties() | 2);
                            } catch (XPathException e) {
                                throw new UncheckedXPathException(e);
                            }
                        }
                        return att;
                    }
                    return att;
                });
            } catch (UncheckedXPathException e) {
                throw e.getXPathException();
            }
        }
        this.nextReceiver.startElement(nameCode, type, atts2, namespaces, location, properties);
    }

    private static boolean isAllAscii(CharSequence value) {
        for (int i = 0; i < value.length(); ++i) {
            if (value.charAt(i) <= '\u007f') continue;
            return false;
        }
        return true;
    }

    static {
        XHTMLURIEscaper.setUrlAttribute("form", "action");
        XHTMLURIEscaper.setUrlAttribute("object", "archive");
        XHTMLURIEscaper.setUrlAttribute("body", "background");
        XHTMLURIEscaper.setUrlAttribute("q", "cite");
        XHTMLURIEscaper.setUrlAttribute("blockquote", "cite");
        XHTMLURIEscaper.setUrlAttribute("del", "cite");
        XHTMLURIEscaper.setUrlAttribute("ins", "cite");
        XHTMLURIEscaper.setUrlAttribute("object", "classid");
        XHTMLURIEscaper.setUrlAttribute("object", "codebase");
        XHTMLURIEscaper.setUrlAttribute("applet", "codebase");
        XHTMLURIEscaper.setUrlAttribute("object", "data");
        XHTMLURIEscaper.setUrlAttribute("button", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("div", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("input", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("object", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("select", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("span", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("table", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("textarea", "datasrc");
        XHTMLURIEscaper.setUrlAttribute("script", "for");
        XHTMLURIEscaper.setUrlAttribute("a", "href");
        XHTMLURIEscaper.setUrlAttribute("a", "name");
        XHTMLURIEscaper.setUrlAttribute("area", "href");
        XHTMLURIEscaper.setUrlAttribute("link", "href");
        XHTMLURIEscaper.setUrlAttribute("base", "href");
        XHTMLURIEscaper.setUrlAttribute("img", "longdesc");
        XHTMLURIEscaper.setUrlAttribute("frame", "longdesc");
        XHTMLURIEscaper.setUrlAttribute("iframe", "longdesc");
        XHTMLURIEscaper.setUrlAttribute("head", "profile");
        XHTMLURIEscaper.setUrlAttribute("script", "src");
        XHTMLURIEscaper.setUrlAttribute("input", "src");
        XHTMLURIEscaper.setUrlAttribute("frame", "src");
        XHTMLURIEscaper.setUrlAttribute("iframe", "src");
        XHTMLURIEscaper.setUrlAttribute("img", "src");
        XHTMLURIEscaper.setUrlAttribute("img", "usemap");
        XHTMLURIEscaper.setUrlAttribute("input", "usemap");
        XHTMLURIEscaper.setUrlAttribute("object", "usemap");
    }
}

