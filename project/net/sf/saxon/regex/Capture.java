/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

public class Capture {
    public int groupNr;
    public int start;
    public int end;

    public Capture(int groupNr, int start, int end) {
        this.groupNr = groupNr;
        this.start = start;
        this.end = end;
    }
}

