/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.util.Strings;

class SSHBuilder {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    SSHBuilder() {
    }

    public void u32(int n) {
        this.bos.write(n >>> 24 & 0xFF);
        this.bos.write(n >>> 16 & 0xFF);
        this.bos.write(n >>> 8 & 0xFF);
        this.bos.write(n & 0xFF);
    }

    public void writeBigNum(BigInteger bigInteger) {
        this.writeBlock(bigInteger.toByteArray());
    }

    public void writeBlock(byte[] byArray) {
        this.u32(byArray.length);
        try {
            this.bos.write(byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException(iOException.getMessage(), iOException);
        }
    }

    public void writeBytes(byte[] byArray) {
        try {
            this.bos.write(byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException(iOException.getMessage(), iOException);
        }
    }

    public void writeString(String string) {
        this.writeBlock(Strings.toByteArray(string));
    }

    public byte[] getBytes() {
        return this.bos.toByteArray();
    }

    public byte[] getPaddedBytes() {
        return this.getPaddedBytes(8);
    }

    public byte[] getPaddedBytes(int n) {
        int n2 = this.bos.size() % n;
        if (0 != n2) {
            int n3 = n - n2;
            for (int i = 1; i <= n3; ++i) {
                this.bos.write(i);
            }
        }
        return this.bos.toByteArray();
    }
}

