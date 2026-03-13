/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

class ExpUtil {
    ExpUtil() {
    }

    static IllegalStateException createIllegalState(String string, Throwable throwable) {
        return new IllegalStateException(string, throwable);
    }
}

