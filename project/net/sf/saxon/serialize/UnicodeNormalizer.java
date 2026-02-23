/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.codenorm.Normalizer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class UnicodeNormalizer
extends ProxyReceiver {
    private Normalizer normalizer;

    public UnicodeNormalizer(String form, Receiver next) throws XPathException {
        super(next);
        int fb;
        switch (form) {
            case "NFC": {
                fb = 2;
                break;
            }
            case "NFD": {
                fb = 0;
                break;
            }
            case "NFKC": {
                fb = 3;
                break;
            }
            case "NFKD": {
                fb = 1;
                break;
            }
            default: {
                XPathException err = new XPathException("Unknown normalization form " + form);
                err.setErrorCode("SESU0011");
                throw err;
            }
        }
        this.normalizer = Normalizer.make(fb, this.getConfiguration());
    }

    public Normalizer getNormalizer() {
        return this.normalizer;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        AttributeMap am2 = attributes.apply(attInfo -> {
            String newValue = this.normalize(attInfo.getValue(), ReceiverOption.contains(attInfo.getProperties(), 256)).toString();
            return new AttributeInfo(attInfo.getNodeName(), attInfo.getType(), newValue, attInfo.getLocation(), attInfo.getProperties());
        });
        this.nextReceiver.startElement(elemName, type, am2, namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (Whitespace.isWhite(chars)) {
            this.nextReceiver.characters(chars, locationId, properties);
        } else {
            this.nextReceiver.characters(this.normalize(chars, ReceiverOption.contains(properties, 256)), locationId, properties);
        }
    }

    public CharSequence normalize(CharSequence in, boolean containsNullMarkers) {
        if (containsNullMarkers) {
            FastStringBuffer out = new FastStringBuffer(in.length());
            String s = in.toString();
            int start = 0;
            int nextNull = s.indexOf(0);
            while (nextNull >= 0) {
                out.cat(this.normalizer.normalize(s.substring(start, nextNull)));
                out.cat('\u0000');
                start = nextNull + 1;
                nextNull = s.indexOf(0, start);
                out.append(s.substring(start, nextNull));
                out.cat('\u0000');
                start = nextNull + 1;
                nextNull = s.indexOf(0, start);
            }
            out.cat(this.normalizer.normalize(s.substring(start)));
            return out.condense();
        }
        return this.normalizer.normalize(in);
    }
}

