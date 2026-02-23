/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.type.AnyExternalObjectType;

public abstract class ExternalObjectType
extends AnyExternalObjectType {
    public abstract String getName();

    public abstract String getTargetNamespace();

    public abstract StructuredQName getTypeName();

    @Override
    public final boolean isPlainType() {
        return false;
    }
}

