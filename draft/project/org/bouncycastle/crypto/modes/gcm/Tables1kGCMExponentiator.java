/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.modes.gcm;

import java.util.Vector;
import org.bouncycastle.crypto.modes.gcm.GCMExponentiator;
import org.bouncycastle.crypto.modes.gcm.GCMUtil;

public class Tables1kGCMExponentiator
implements GCMExponentiator {
    private Vector lookupPowX2;

    public void init(byte[] byArray) {
        long[] lArray = GCMUtil.asLongs(byArray);
        if (this.lookupPowX2 != null && 0L != GCMUtil.areEqual(lArray, (long[])this.lookupPowX2.elementAt(0))) {
            return;
        }
        this.lookupPowX2 = new Vector(8);
        this.lookupPowX2.addElement(lArray);
    }

    public void exponentiateX(long l, byte[] byArray) {
        long[] lArray = GCMUtil.oneAsLongs();
        int n = 0;
        while (l > 0L) {
            if ((l & 1L) != 0L) {
                this.ensureAvailable(n);
                GCMUtil.multiply(lArray, (long[])this.lookupPowX2.elementAt(n));
            }
            ++n;
            l >>>= 1;
        }
        GCMUtil.asBytes(lArray, byArray);
    }

    private void ensureAvailable(int n) {
        int n2 = this.lookupPowX2.size() - 1;
        if (n2 < n) {
            long[] lArray = (long[])this.lookupPowX2.elementAt(n2);
            do {
                long[] lArray2 = new long[2];
                GCMUtil.square(lArray, lArray2);
                this.lookupPowX2.addElement(lArray2);
                lArray = lArray2;
            } while (++n2 < n);
        }
    }
}

