/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.HashMap;

public class Instrumentation {
    public static final boolean ACTIVE = false;
    public static HashMap<String, Long> counters = new HashMap();

    public static void count(String counter) {
        if (counters.containsKey(counter)) {
            counters.put(counter, counters.get(counter) + 1L);
        } else {
            counters.put(counter, 1L);
        }
    }

    public static void count(String counter, long increment) {
        if (counters.containsKey(counter)) {
            counters.put(counter, counters.get(counter) + increment);
        } else {
            counters.put(counter, increment);
        }
    }

    public static void report() {
    }

    public static void reset() {
        counters.clear();
    }
}

