/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.function.BiConsumer;
import net.sf.saxon.expr.Locatable;
import net.sf.saxon.om.StructuredQName;

public interface Traceable
extends Locatable {
    public StructuredQName getObjectName();

    default public void gatherProperties(BiConsumer<String, Object> consumer) {
    }
}

