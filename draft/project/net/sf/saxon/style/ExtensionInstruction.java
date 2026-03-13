/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.style.StyleElement;

public abstract class ExtensionInstruction
extends StyleElement {
    @Override
    public final boolean isInstruction() {
        return true;
    }

    @Override
    public final boolean mayContainFallback() {
        return true;
    }
}

