/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.EnumSet;

public class EnumSetTool {
    public static <P extends Enum<P>> EnumSet<P> intersect(EnumSet<P> a, EnumSet<P> b) {
        EnumSet<P> e = EnumSet.copyOf(a);
        e.retainAll(b);
        return e;
    }

    public static <P extends Enum<P>> EnumSet<P> union(EnumSet<P> a, EnumSet<P> b) {
        EnumSet<P> e = EnumSet.copyOf(a);
        e.addAll(b);
        return e;
    }
}

