/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

public class DocumentNumberAllocator {
    private long nextDocumentNumber = 0L;
    private long nextStreamedDocumentNumber = -2L;

    public synchronized long allocateDocumentNumber() {
        return this.nextDocumentNumber++;
    }

    public synchronized long allocateStreamedDocumentNumber() {
        return this.nextStreamedDocumentNumber--;
    }
}

