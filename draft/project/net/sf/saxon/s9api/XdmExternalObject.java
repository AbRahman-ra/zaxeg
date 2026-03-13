/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.value.ExternalObject;
import net.sf.saxon.value.ObjectValue;

public class XdmExternalObject
extends XdmItem {
    private XdmExternalObject() {
    }

    public XdmExternalObject(Object value) {
        super(value instanceof ObjectValue ? (ObjectValue<Object>)value : new ObjectValue<Object>(value));
    }

    public Object getExternalObject() {
        return ((ExternalObject)this.getUnderlyingValue()).getObject();
    }

    @Override
    public String toString() {
        return this.getExternalObject().toString();
    }

    public boolean equals(Object other) {
        return other instanceof XdmExternalObject && this.getUnderlyingValue().equals(((XdmExternalObject)other).getUnderlyingValue());
    }

    public int hashCode() {
        return this.getUnderlyingValue().hashCode();
    }
}

