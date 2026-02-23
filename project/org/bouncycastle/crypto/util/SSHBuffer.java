/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.math.BigInteger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

class SSHBuffer {
    private final byte[] buffer;
    private int pos = 0;

    public SSHBuffer(byte[] byArray, byte[] byArray2) {
        this.buffer = byArray2;
        for (int i = 0; i != byArray.length; ++i) {
            if (byArray[i] == byArray2[i]) continue;
            throw new IllegalArgumentException("magic-number incorrect");
        }
        this.pos += byArray.length;
    }

    public SSHBuffer(byte[] byArray) {
        this.buffer = byArray;
    }

    public int readU32() {
        if (this.pos > this.buffer.length - 4) {
            throw new IllegalArgumentException("4 bytes for U32 exceeds buffer.");
        }
        int n = (this.buffer[this.pos++] & 0xFF) << 24;
        n |= (this.buffer[this.pos++] & 0xFF) << 16;
        n |= (this.buffer[this.pos++] & 0xFF) << 8;
        return n |= this.buffer[this.pos++] & 0xFF;
    }

    public String readString() {
        return Strings.fromByteArray(this.readBlock());
    }

    public byte[] readBlock() {
        int n = this.readU32();
        if (n == 0) {
            return new byte[0];
        }
        if (this.pos > this.buffer.length - n) {
            throw new IllegalArgumentException("not enough data for block");
        }
        int n2 = this.pos;
        this.pos += n;
        return Arrays.copyOfRange(this.buffer, n2, this.pos);
    }

    public void skipBlock() {
        int n = this.readU32();
        if (this.pos > this.buffer.length - n) {
            throw new IllegalArgumentException("not enough data for block");
        }
        this.pos += n;
    }

    public byte[] readPaddedBlock() {
        return this.readPaddedBlock(8);
    }

    public byte[] readPaddedBlock(int n) {
        int n2;
        int n3 = this.readU32();
        if (n3 == 0) {
            return new byte[0];
        }
        if (this.pos > this.buffer.length - n3) {
            throw new IllegalArgumentException("not enough data for block");
        }
        int n4 = n3 % n;
        if (0 != n4) {
            throw new IllegalArgumentException("missing padding");
        }
        int n5 = this.pos;
        this.pos += n3;
        int n6 = this.pos;
        if (n3 > 0 && 0 < (n2 = this.buffer[this.pos - 1] & 0xFF) && n2 < n) {
            int n7 = n2;
            int n8 = 1;
            int n9 = n6 -= n7;
            while (n8 <= n7) {
                if (n8 != (this.buffer[n9] & 0xFF)) {
                    throw new IllegalArgumentException("incorrect padding");
                }
                ++n8;
                ++n9;
            }
        }
        return Arrays.copyOfRange(this.buffer, n5, n6);
    }

    public BigInteger readBigNumPositive() {
        int n = this.readU32();
        if (this.pos + n > this.buffer.length) {
            throw new IllegalArgumentException("not enough data for big num");
        }
        int n2 = this.pos;
        this.pos += n;
        byte[] byArray = Arrays.copyOfRange(this.buffer, n2, this.pos);
        return new BigInteger(1, byArray);
    }

    public byte[] getBuffer() {
        return Arrays.clone(this.buffer);
    }

    public boolean hasRemaining() {
        return this.pos < this.buffer.length;
    }
}

