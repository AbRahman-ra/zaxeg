/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;
import org.bouncycastle.math.ec.rfc7748.X448;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class X448PrivateKeyParameters
extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 56;
    public static final int SECRET_SIZE = 56;
    private final byte[] data = new byte[56];

    public X448PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        X448.generatePrivateKey(secureRandom, this.data);
    }

    public X448PrivateKeyParameters(byte[] byArray) {
        this(X448PrivateKeyParameters.validate(byArray), 0);
    }

    public X448PrivateKeyParameters(byte[] byArray, int n) {
        super(true);
        System.arraycopy(byArray, n, this.data, 0, 56);
    }

    public X448PrivateKeyParameters(InputStream inputStream) throws IOException {
        super(true);
        if (56 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of X448 private key");
        }
    }

    public void encode(byte[] byArray, int n) {
        System.arraycopy(this.data, 0, byArray, n, 56);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    public X448PublicKeyParameters generatePublicKey() {
        byte[] byArray = new byte[56];
        X448.generatePublicKey(this.data, 0, byArray, 0);
        return new X448PublicKeyParameters(byArray, 0);
    }

    public void generateSecret(X448PublicKeyParameters x448PublicKeyParameters, byte[] byArray, int n) {
        byte[] byArray2 = new byte[56];
        x448PublicKeyParameters.encode(byArray2, 0);
        if (!X448.calculateAgreement(this.data, 0, byArray2, 0, byArray, n)) {
            throw new IllegalStateException("X448 agreement failed");
        }
    }

    private static byte[] validate(byte[] byArray) {
        if (byArray.length != 56) {
            throw new IllegalArgumentException("'buf' must have length 56");
        }
        return byArray;
    }
}

