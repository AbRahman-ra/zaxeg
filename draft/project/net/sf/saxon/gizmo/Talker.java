/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.gizmo;

import java.util.List;

public interface Talker {
    public String exchange(String var1);

    default public void setAutoCompletion(List<String> candidates) {
    }
}

