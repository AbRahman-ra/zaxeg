/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.digests;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.Arrays;

public class NullDigest
implements Digest {
    private OpenByteArrayOutputStream bOut = new OpenByteArrayOutputStream();

    public String getAlgorithmName() {
        return "NULL";
    }

    public int getDigestSize() {
        return this.bOut.size();
    }

    public void update(byte by) {
        this.bOut.write(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.bOut.write(byArray, n, n2);
    }

    public int doFinal(byte[] byArray, int n) {
        int n2 = this.bOut.size();
        this.bOut.copy(byArray, n);
        this.reset();
        return n2;
    }

    public void reset() {
        this.bOut.reset();
    }

    private static class OpenByteArrayOutputStream
    extends ByteArrayOutputStream {
        private OpenByteArrayOutputStream() {
        }

        public void reset() {
            super.reset();
            Arrays.clear(this.buf);
        }

        void copy(byte[] byArray, int n) {
            System.arraycopy(this.buf, 0, byArray, n, this.size());
        }
    }
}

