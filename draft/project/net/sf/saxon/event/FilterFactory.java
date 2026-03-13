/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Receiver;

@FunctionalInterface
public interface FilterFactory {
    public Receiver makeFilter(Receiver var1);
}

