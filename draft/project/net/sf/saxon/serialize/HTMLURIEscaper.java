/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.HTMLTagHashSet;
import net.sf.saxon.serialize.charcode.UTF8CharacterSet;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;

public class HTMLURIEscaper
extends ProxyReceiver {
    private static HTMLTagHashSet urlAttributes = new HTMLTagHashSet(47);
    private static HTMLTagHashSet urlCombinations = new HTMLTagHashSet(101);
    protected NodeName currentElement;
    protected boolean escapeURIAttributes = true;
    protected NamePool pool;

    private static void setUrlAttribute(String element, String attribute) {
        urlAttributes.add(attribute);
        urlCombinations.add(element + '+' + attribute);
    }

    public boolean isUrlAttribute(NodeName element, NodeName attribute) {
        String attributeName;
        if (this.pool == null) {
            this.pool = this.getNamePool();
        }
        if (!urlAttributes.contains(attributeName = attribute.getDisplayName())) {
            return false;
        }
        String elementName = element.getDisplayName();
        return urlCombinations.contains(elementName + '+' + attributeName);
    }

    public HTMLURIEscaper(Receiver nextReceiver) {
        super(nextReceiver);
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.nextReceiver.startDocument(properties);
        this.pool = this.getPipelineConfiguration().getConfiguration().getNamePool();
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
                                return new AttributeInfo(att.getNodeName(), att.getType(), HTMLURIEscaper.escapeURL(value, true, this.getConfiguration()).toString(), att.getLocation(), att.getProperties() | 2);
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

    public static CharSequence escapeURL(CharSequence url, boolean normalize, Configuration config) throws XPathException {
        for (int i = url.length() - 1; i >= 0; --i) {
            char ch = url.charAt(i);
            if (ch >= ' ' && ch <= '~') continue;
            if (normalize) {
                CharSequence normalized = Normalizer.make(2, config).normalize(url);
                return HTMLURIEscaper.reallyEscapeURL(normalized);
            }
            return HTMLURIEscaper.reallyEscapeURL(url);
        }
        return url;
    }

    private static CharSequence reallyEscapeURL(CharSequence url) {
        FastStringBuffer sb = new FastStringBuffer(url.length() + 20);
        String hex = "0123456789ABCDEF";
        byte[] array = new byte[4];
        for (int i = 0; i < url.length(); ++i) {
            char ch = url.charAt(i);
            if (ch < ' ' || ch > '~') {
                int used = UTF8CharacterSet.getUTF8Encoding(ch, i + 1 < url.length() ? url.charAt(i + 1) : (char)' ', array);
                for (int b = 0; b < used; ++b) {
                    int v = array[b] & 0xFF;
                    sb.cat('%');
                    sb.cat("0123456789ABCDEF".charAt(v / 16));
                    sb.cat("0123456789ABCDEF".charAt(v % 16));
                }
                continue;
            }
            sb.cat(ch);
        }
        return sb;
    }

    static {
        HTMLURIEscaper.setUrlAttribute("form", "action");
        HTMLURIEscaper.setUrlAttribute("object", "archive");
        HTMLURIEscaper.setUrlAttribute("body", "background");
        HTMLURIEscaper.setUrlAttribute("q", "cite");
        HTMLURIEscaper.setUrlAttribute("blockquote", "cite");
        HTMLURIEscaper.setUrlAttribute("del", "cite");
        HTMLURIEscaper.setUrlAttribute("ins", "cite");
        HTMLURIEscaper.setUrlAttribute("object", "classid");
        HTMLURIEscaper.setUrlAttribute("object", "codebase");
        HTMLURIEscaper.setUrlAttribute("applet", "codebase");
        HTMLURIEscaper.setUrlAttribute("object", "data");
        HTMLURIEscaper.setUrlAttribute("button", "datasrc");
        HTMLURIEscaper.setUrlAttribute("div", "datasrc");
        HTMLURIEscaper.setUrlAttribute("input", "datasrc");
        HTMLURIEscaper.setUrlAttribute("object", "datasrc");
        HTMLURIEscaper.setUrlAttribute("select", "datasrc");
        HTMLURIEscaper.setUrlAttribute("span", "datasrc");
        HTMLURIEscaper.setUrlAttribute("table", "datasrc");
        HTMLURIEscaper.setUrlAttribute("textarea", "datasrc");
        HTMLURIEscaper.setUrlAttribute("script", "for");
        HTMLURIEscaper.setUrlAttribute("a", "href");
        HTMLURIEscaper.setUrlAttribute("a", "name");
        HTMLURIEscaper.setUrlAttribute("area", "href");
        HTMLURIEscaper.setUrlAttribute("link", "href");
        HTMLURIEscaper.setUrlAttribute("base", "href");
        HTMLURIEscaper.setUrlAttribute("img", "longdesc");
        HTMLURIEscaper.setUrlAttribute("frame", "longdesc");
        HTMLURIEscaper.setUrlAttribute("iframe", "longdesc");
        HTMLURIEscaper.setUrlAttribute("head", "profile");
        HTMLURIEscaper.setUrlAttribute("script", "src");
        HTMLURIEscaper.setUrlAttribute("input", "src");
        HTMLURIEscaper.setUrlAttribute("frame", "src");
        HTMLURIEscaper.setUrlAttribute("iframe", "src");
        HTMLURIEscaper.setUrlAttribute("img", "src");
        HTMLURIEscaper.setUrlAttribute("img", "usemap");
        HTMLURIEscaper.setUrlAttribute("input", "usemap");
        HTMLURIEscaper.setUrlAttribute("object", "usemap");
    }
}

