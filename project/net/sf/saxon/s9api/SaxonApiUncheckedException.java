/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

public class SaxonApiUncheckedException
extends RuntimeException {
    public SaxonApiUncheckedException(Throwable err) {
        super(err);
    }

    @Override
    public String getMessage() {
        return this.getCause().getMessage();
    }
}

