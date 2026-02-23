/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class RepairingContentHandler
extends XMLFilterImpl {
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (uri != null && !uri.isEmpty() && !qName.contains(":")) {
            this.startPrefixMapping("", uri);
        }
        super.startElement(uri, localName, qName, atts);
    }
}

