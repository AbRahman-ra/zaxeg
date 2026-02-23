/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

public class Timer {
    private long start;
    private long prev;

    public Timer() {
        this.prev = this.start = System.nanoTime();
    }

    public void report(String label) {
        long time = System.nanoTime();
        System.err.println(label + " " + (double)(time - this.prev) / 1000000.0 + "ms");
        this.prev = time;
    }

    public void reportCumulative(String label) {
        long time = System.nanoTime();
        System.err.println(label + " " + (double)(time - this.start) / 1000000.0 + "ms");
        this.prev = time;
    }
}

