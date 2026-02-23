/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.value.SequenceType;

public interface TupleType
extends FunctionItemType {
    public Iterable<String> getFieldNames();

    public SequenceType getFieldType(String var1);

    public boolean isExtensible();
}

