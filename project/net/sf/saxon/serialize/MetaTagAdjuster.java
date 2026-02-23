/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Properties;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;

public class MetaTagAdjuster
extends ProxyReceiver {
    private boolean seekingHead = true;
    private int droppingMetaTags = -1;
    private boolean inMetaTag = false;
    String encoding;
    private String mediaType;
    private int level = 0;
    private boolean isXHTML = false;
    private int htmlVersion = 4;

    public MetaTagAdjuster(Receiver next) {
        super(next);
    }

    public void setOutputProperties(Properties details) {
        String htmlVn;
        this.encoding = details.getProperty("encoding");
        if (this.encoding == null) {
            this.encoding = "UTF-8";
        }
        this.mediaType = details.getProperty("media-type");
        if (this.mediaType == null) {
            this.mediaType = "text/html";
        }
        if ((htmlVn = details.getProperty("html-version")) == null && !this.isXHTML) {
            htmlVn = details.getProperty("version");
        }
        if (htmlVn != null && htmlVn.startsWith("5")) {
            this.htmlVersion = 5;
        }
    }

    public void setIsXHTML(boolean xhtml) {
        this.isXHTML = xhtml;
    }

    private boolean comparesEqual(String name1, String name2) {
        if (this.isXHTML) {
            return name1.equals(name2);
        }
        return name1.equalsIgnoreCase(name2);
    }

    private boolean matchesName(NodeName name, String local) {
        if (this.isXHTML) {
            if (!name.getLocalPart().equals(local)) {
                return false;
            }
            if (this.htmlVersion == 5) {
                return name.hasURI("") || name.hasURI("http://www.w3.org/1999/xhtml");
            }
            return name.hasURI("http://www.w3.org/1999/xhtml");
        }
        return name.getLocalPart().equalsIgnoreCase(local);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        if (this.droppingMetaTags == this.level && this.matchesName(elemName, "meta")) {
            boolean found = false;
            for (AttributeInfo att : attributes) {
                String value;
                String name = att.getNodeName().getLocalPart();
                if (!this.comparesEqual(name, "http-equiv") || !(value = Whitespace.trim(att.getValue())).equalsIgnoreCase("Content-Type")) continue;
                found = true;
                break;
            }
            this.inMetaTag = found;
            if (found) {
                return;
            }
        }
        ++this.level;
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        if (this.seekingHead && this.matchesName(elemName, "head")) {
            String headPrefix = elemName.getPrefix();
            String headURI = elemName.getURI();
            FingerprintedQName metaCode = new FingerprintedQName(headPrefix, headURI, "meta");
            AttributeMap atts = EmptyAttributeMap.getInstance();
            atts = atts.put(new AttributeInfo(new NoNamespaceName("http-equiv"), BuiltInAtomicType.UNTYPED_ATOMIC, "Content-Type", Loc.NONE, 0));
            atts = atts.put(new AttributeInfo(new NoNamespaceName("content"), BuiltInAtomicType.UNTYPED_ATOMIC, this.mediaType + "; charset=" + this.encoding, Loc.NONE, 0));
            this.nextReceiver.startElement(metaCode, Untyped.getInstance(), atts, namespaces, location, 0);
            this.droppingMetaTags = this.level;
            this.seekingHead = false;
            this.nextReceiver.endElement();
        }
    }

    @Override
    public void endElement() throws XPathException {
        if (this.inMetaTag) {
            this.inMetaTag = false;
        } else {
            --this.level;
            if (this.droppingMetaTags == this.level + 1) {
                this.droppingMetaTags = -1;
            }
            this.nextReceiver.endElement();
        }
    }
}

