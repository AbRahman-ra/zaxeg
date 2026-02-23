/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.Arrays;

public class LineNumberMap {
    private int[] sequenceNumbers = new int[200];
    private int[] lineNumbers = new int[200];
    private int[] columnNumbers = new int[200];
    private int allocated = 0;

    public void setLineAndColumn(int sequence, int line, int column) {
        if (this.sequenceNumbers.length <= this.allocated + 1) {
            this.sequenceNumbers = Arrays.copyOf(this.sequenceNumbers, this.allocated * 2);
            this.lineNumbers = Arrays.copyOf(this.lineNumbers, this.allocated * 2);
            this.columnNumbers = Arrays.copyOf(this.columnNumbers, this.allocated * 2);
        }
        this.sequenceNumbers[this.allocated] = sequence;
        this.lineNumbers[this.allocated] = line;
        this.columnNumbers[this.allocated] = column;
        ++this.allocated;
    }

    public int getLineNumber(int sequence) {
        int index;
        if (this.sequenceNumbers.length > this.allocated) {
            this.condense();
        }
        if ((index = Arrays.binarySearch(this.sequenceNumbers, sequence)) < 0 && (index = -index - 1) > this.lineNumbers.length - 1) {
            index = this.lineNumbers.length - 1;
        }
        return this.lineNumbers[index];
    }

    public int getColumnNumber(int sequence) {
        int index;
        if (this.sequenceNumbers.length > this.allocated) {
            this.condense();
        }
        if ((index = Arrays.binarySearch(this.sequenceNumbers, sequence)) < 0 && (index = -index - 1) >= this.columnNumbers.length) {
            index = this.columnNumbers.length - 1;
        }
        return this.columnNumbers[index];
    }

    private synchronized void condense() {
        this.sequenceNumbers = Arrays.copyOf(this.sequenceNumbers, this.allocated);
        this.lineNumbers = Arrays.copyOf(this.lineNumbers, this.allocated);
        this.columnNumbers = Arrays.copyOf(this.columnNumbers, this.allocated);
    }
}

