/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class X25519PrivateKeyParameters
extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 32;
    public static final int SECRET_SIZE = 32;
    private final byte[] data = new byte[32];

    public X25519PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        X25519.generatePrivateKey(secureRandom, this.data);
    }

    public X25519PrivateKeyParameters(byte[] byArray) {
        this(X25519PrivateKeyParameters.validate(byArray), 0);
    }

    public X25519PrivateKeyParameters(byte[] byArray, int n) {
        super(true);
        System.arraycopy(byArray, n, this.data, 0, 32);
    }

    public X25519PrivateKeyParameters(InputStream inputStream) throws IOException {
        super(true);
        if (32 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of X25519 private key");
        }
    }

    public void encode(byte[] byArray, int n) {
        System.arraycopy(this.data, 0, byArray, n, 32);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    public X25519PublicKeyParameters generatePublicKey() {
        byte[] byArray = new byte[32];
        X25519.generatePublicKey(this.data, 0, byArray, 0);
        return new X25519PublicKeyParameters(byArray, 0);
    }

    public void generateSecret(X25519PublicKeyParameters x25519PublicKeyParameters, byte[] byArray, int n) {
        byte[] byArray2 = new byte[32];
        x25519PublicKeyParameters.encode(byArray2, 0);
        if (!X25519.calculateAgreement(this.data, 0, byArray2, 0, byArray, n)) {
            throw new IllegalStateException("X25519 agreement failed");
        }
    }

    private static byte[] validate(byte[] byArray) {
        if (byArray.length != 32) {
            throw new IllegalArgumentException("'buf' must have length 32");
        }
        return byArray;
    }
}

