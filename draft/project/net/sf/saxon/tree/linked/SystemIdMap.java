/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.Arrays;

public class SystemIdMap {
    private int[] sequenceNumbers = new int[4];
    private String[] uris = new String[4];
    private int allocated = 0;

    public void setSystemId(int sequence, String uri) {
        if (this.allocated > 0) {
            if (uri.equals(this.uris[this.allocated - 1])) {
                return;
            }
            if (sequence <= this.sequenceNumbers[this.allocated - 1]) {
                throw new IllegalArgumentException("System IDs of nodes are immutable");
            }
        }
        if (this.sequenceNumbers.length <= this.allocated + 1) {
            this.sequenceNumbers = Arrays.copyOf(this.sequenceNumbers, this.allocated * 2);
            this.uris = Arrays.copyOf(this.uris, this.allocated * 2);
        }
        this.sequenceNumbers[this.allocated] = sequence;
        this.uris[this.allocated] = uri;
        ++this.allocated;
    }

    public String getSystemId(int sequence) {
        if (this.allocated == 0) {
            return null;
        }
        for (int i = 1; i < this.allocated; ++i) {
            if (this.sequenceNumbers[i] <= sequence) continue;
            return this.uris[i - 1];
        }
        return this.uris[this.allocated - 1];
    }
}

