/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.util.Encodable;

public class Composer {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    private Composer() {
    }

    public static Composer compose() {
        return new Composer();
    }

    public Composer u64str(long l) {
        this.u32str((int)(l >>> 32));
        this.u32str((int)l);
        return this;
    }

    public Composer u32str(int n) {
        this.bos.write((byte)(n >>> 24));
        this.bos.write((byte)(n >>> 16));
        this.bos.write((byte)(n >>> 8));
        this.bos.write((byte)n);
        return this;
    }

    public Composer u16str(int n) {
        this.bos.write((byte)((n &= 0xFFFF) >>> 8));
        this.bos.write((byte)n);
        return this;
    }

    public Composer bytes(Encodable[] encodableArray) {
        try {
            for (Encodable encodable : encodableArray) {
                this.bos.write(encodable.getEncoded());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return this;
    }

    public Composer bytes(Encodable encodable) {
        try {
            this.bos.write(encodable.getEncoded());
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return this;
    }

    public Composer pad(int n, int n2) {
        while (n2 >= 0) {
            try {
                this.bos.write(n);
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
            --n2;
        }
        return this;
    }

    public Composer bytes(byte[][] byArray) {
        try {
            for (byte[] byArray2 : byArray) {
                this.bos.write(byArray2);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return this;
    }

    public Composer bytes(byte[][] byArray, int n, int n2) {
        try {
            for (int i = n; i != n2; ++i) {
                this.bos.write(byArray[i]);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return this;
    }

    public Composer bytes(byte[] byArray) {
        try {
            this.bos.write(byArray);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return this;
    }

    public Composer bytes(byte[] byArray, int n, int n2) {
        try {
            this.bos.write(byArray, n, n2);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return this;
    }

    public byte[] build() {
        return this.bos.toByteArray();
    }

    public Composer padUntil(int n, int n2) {
        while (this.bos.size() < n2) {
            this.bos.write(n);
        }
        return this;
    }

    public Composer bool(boolean bl) {
        this.bos.write(bl ? 1 : 0);
        return this;
    }
}

