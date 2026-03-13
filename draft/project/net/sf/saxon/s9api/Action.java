/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.SaxonApiException;

@FunctionalInterface
public interface Action {
    public void act() throws SaxonApiException;
}

