/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.crypto.util.PBKDFConfig;

public class ScryptConfig
extends PBKDFConfig {
    private final int costParameter;
    private final int blockSize;
    private final int parallelizationParameter;
    private final int saltLength;

    private ScryptConfig(Builder builder) {
        super(MiscObjectIdentifiers.id_scrypt);
        this.costParameter = builder.costParameter;
        this.blockSize = builder.blockSize;
        this.parallelizationParameter = builder.parallelizationParameter;
        this.saltLength = builder.saltLength;
    }

    public int getCostParameter() {
        return this.costParameter;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getParallelizationParameter() {
        return this.parallelizationParameter;
    }

    public int getSaltLength() {
        return this.saltLength;
    }

    public static class Builder {
        private final int costParameter;
        private final int blockSize;
        private final int parallelizationParameter;
        private int saltLength = 16;

        public Builder(int n, int n2, int n3) {
            if (n <= 1 || !Builder.isPowerOf2(n)) {
                throw new IllegalArgumentException("Cost parameter N must be > 1 and a power of 2");
            }
            this.costParameter = n;
            this.blockSize = n2;
            this.parallelizationParameter = n3;
        }

        public Builder withSaltLength(int n) {
            this.saltLength = n;
            return this;
        }

        public ScryptConfig build() {
            return new ScryptConfig(this);
        }

        private static boolean isPowerOf2(int n) {
            return (n & n - 1) == 0;
        }
    }
}

