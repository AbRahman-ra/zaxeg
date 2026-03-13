/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SimpleType;

public class AttributeInfo {
    private NodeName nodeName;
    private SimpleType type;
    private String value;
    private Location location;
    private int properties;

    public AttributeInfo(NodeName nodeName, SimpleType type, String value, Location location, int properties) {
        this.nodeName = nodeName;
        this.type = type;
        this.value = value;
        this.location = location;
        this.properties = properties;
    }

    public NodeName getNodeName() {
        return this.nodeName;
    }

    public SimpleType getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public Location getLocation() {
        return this.location;
    }

    public int getProperties() {
        return this.properties;
    }

    public boolean isId() {
        try {
            return StandardNames.XML_ID_NAME.equals(this.nodeName) || ReceiverOption.contains(this.getProperties(), 2048) || this.getType().isIdType();
        } catch (MissingComponentException e) {
            return false;
        }
    }

    public AttributeInfo withNodeName(NodeName newName) {
        return new AttributeInfo(newName, this.type, this.value, this.location, this.properties);
    }

    public static class Deleted
    extends AttributeInfo {
        public Deleted(AttributeInfo att) {
            super(att.getNodeName(), att.getType(), att.getValue(), att.getLocation(), att.getProperties());
        }
    }
}

