/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CharToByteConverter;
import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.util.Arrays;

public class Argon2Parameters {
    public static final int ARGON2_d = 0;
    public static final int ARGON2_i = 1;
    public static final int ARGON2_id = 2;
    public static final int ARGON2_VERSION_10 = 16;
    public static final int ARGON2_VERSION_13 = 19;
    private static final int DEFAULT_ITERATIONS = 3;
    private static final int DEFAULT_MEMORY_COST = 12;
    private static final int DEFAULT_LANES = 1;
    private static final int DEFAULT_TYPE = 1;
    private static final int DEFAULT_VERSION = 19;
    private final byte[] salt;
    private final byte[] secret;
    private final byte[] additional;
    private final int iterations;
    private final int memory;
    private final int lanes;
    private final int version;
    private final int type;
    private final CharToByteConverter converter;

    private Argon2Parameters(int n, byte[] byArray, byte[] byArray2, byte[] byArray3, int n2, int n3, int n4, int n5, CharToByteConverter charToByteConverter) {
        this.salt = Arrays.clone(byArray);
        this.secret = Arrays.clone(byArray2);
        this.additional = Arrays.clone(byArray3);
        this.iterations = n2;
        this.memory = n3;
        this.lanes = n4;
        this.version = n5;
        this.type = n;
        this.converter = charToByteConverter;
    }

    public byte[] getSalt() {
        return Arrays.clone(this.salt);
    }

    public byte[] getSecret() {
        return Arrays.clone(this.secret);
    }

    public byte[] getAdditional() {
        return Arrays.clone(this.additional);
    }

    public int getIterations() {
        return this.iterations;
    }

    public int getMemory() {
        return this.memory;
    }

    public int getLanes() {
        return this.lanes;
    }

    public int getVersion() {
        return this.version;
    }

    public int getType() {
        return this.type;
    }

    public CharToByteConverter getCharToByteConverter() {
        return this.converter;
    }

    public void clear() {
        Arrays.clear(this.salt);
        Arrays.clear(this.secret);
        Arrays.clear(this.additional);
    }

    public static class Builder {
        private byte[] salt;
        private byte[] secret;
        private byte[] additional;
        private int iterations;
        private int memory;
        private int lanes;
        private int version;
        private final int type;
        private CharToByteConverter converter = PasswordConverter.UTF8;

        public Builder() {
            this(1);
        }

        public Builder(int n) {
            this.type = n;
            this.lanes = 1;
            this.memory = 4096;
            this.iterations = 3;
            this.version = 19;
        }

        public Builder withParallelism(int n) {
            this.lanes = n;
            return this;
        }

        public Builder withSalt(byte[] byArray) {
            this.salt = Arrays.clone(byArray);
            return this;
        }

        public Builder withSecret(byte[] byArray) {
            this.secret = Arrays.clone(byArray);
            return this;
        }

        public Builder withAdditional(byte[] byArray) {
            this.additional = Arrays.clone(byArray);
            return this;
        }

        public Builder withIterations(int n) {
            this.iterations = n;
            return this;
        }

        public Builder withMemoryAsKB(int n) {
            this.memory = n;
            return this;
        }

        public Builder withMemoryPowOfTwo(int n) {
            this.memory = 1 << n;
            return this;
        }

        public Builder withVersion(int n) {
            this.version = n;
            return this;
        }

        public Builder withCharToByteConverter(CharToByteConverter charToByteConverter) {
            this.converter = charToByteConverter;
            return this;
        }

        public Argon2Parameters build() {
            return new Argon2Parameters(this.type, this.salt, this.secret, this.additional, this.iterations, this.memory, this.lanes, this.version, this.converter);
        }

        public void clear() {
            Arrays.clear(this.salt);
            Arrays.clear(this.secret);
            Arrays.clear(this.additional);
        }
    }
}

