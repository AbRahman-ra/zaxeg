/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.util.Comparator;

class ByteArrayComparator
implements Comparator {
    ByteArrayComparator() {
    }

    public int compare(Object object, Object object2) {
        byte[] byArray = (byte[])object;
        byte[] byArray2 = (byte[])object2;
        for (int i = 0; i < byArray.length && i < byArray2.length; ++i) {
            int n = byArray[i] & 0xFF;
            int n2 = byArray2[i] & 0xFF;
            if (n == n2) continue;
            return n - n2;
        }
        return byArray.length - byArray2.length;
    }
}

