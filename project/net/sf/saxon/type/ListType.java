/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.type.CastingTarget;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SimpleType;

public interface ListType
extends SimpleType,
CastingTarget {
    public SimpleType getItemType() throws MissingComponentException;
}

