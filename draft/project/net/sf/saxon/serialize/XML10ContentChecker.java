/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.serialize.charcode.XMLCharacterData;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class XML10ContentChecker
extends ProxyReceiver {
    public XML10ContentChecker(Receiver next) {
        super(next);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        for (AttributeInfo att : attributes) {
            this.checkString(att.getValue(), att.getLocation());
        }
        this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.checkString(chars, locationId);
        this.nextReceiver.characters(chars, locationId, properties);
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.checkString(chars, locationId);
        this.nextReceiver.comment(chars, locationId, properties);
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.checkString(data, locationId);
        this.nextReceiver.processingInstruction(target, data, locationId, properties);
    }

    private void checkString(CharSequence in, Location locationId) throws XPathException {
        int len = in.length();
        for (int c = 0; c < len; ++c) {
            int ch32 = in.charAt(c);
            if (UTF16CharacterSet.isHighSurrogate(ch32)) {
                char low = in.charAt(++c);
                ch32 = UTF16CharacterSet.combinePair((char)ch32, low);
            }
            if (XMLCharacterData.isValid10(ch32)) continue;
            XPathException err = new XPathException("The result tree contains a character not allowed by XML 1.0 (hex " + Integer.toHexString(ch32) + ')');
            err.setErrorCode("SERE0006");
            err.setLocator(locationId);
            throw err;
        }
    }
}

