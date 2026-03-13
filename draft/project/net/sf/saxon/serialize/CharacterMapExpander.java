/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.ArrayList;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class CharacterMapExpander
extends ProxyReceiver {
    private CharacterMap charMap;
    private boolean useNullMarkers = true;

    public CharacterMapExpander(Receiver next) {
        super(next);
    }

    public void setCharacterMap(CharacterMap map) {
        this.charMap = map;
    }

    public CharacterMap getCharacterMap() {
        return this.charMap;
    }

    public void setUseNullMarkers(boolean use) {
        this.useNullMarkers = use;
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        ArrayList<AttributeInfo> atts2 = new ArrayList<AttributeInfo>(attributes.size());
        for (AttributeInfo att : attributes) {
            String oldValue = att.getValue();
            if (!ReceiverOption.contains(att.getProperties(), 2)) {
                CharSequence mapped = this.charMap.map(oldValue, this.useNullMarkers);
                if (mapped != oldValue) {
                    int p2 = (att.getProperties() | 0x100) & 0xFFFFFFFB;
                    atts2.add(new AttributeInfo(att.getNodeName(), att.getType(), mapped.toString(), att.getLocation(), p2));
                    continue;
                }
                atts2.add(att);
                continue;
            }
            atts2.add(att);
        }
        this.nextReceiver.startElement(elemName, type, AttributeMap.fromList(atts2), namespaces, location, properties);
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (!ReceiverOption.contains(properties, 2)) {
            CharSequence mapped = this.charMap.map(chars, this.useNullMarkers);
            if (mapped != chars) {
                properties = (properties | 0x100) & 0xFFFFFFFB;
            }
            this.nextReceiver.characters(mapped, locationId, properties);
        } else {
            this.nextReceiver.characters(chars, locationId, properties);
        }
    }
}

