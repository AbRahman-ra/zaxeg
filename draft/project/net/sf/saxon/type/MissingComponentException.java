/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

public abstract class MissingComponentException
extends RuntimeException {
    public MissingComponentException(String ref) {
        super(ref);
    }
}

