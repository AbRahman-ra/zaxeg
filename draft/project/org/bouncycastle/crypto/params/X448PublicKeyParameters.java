/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class X448PublicKeyParameters
extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 56;
    private final byte[] data = new byte[56];

    public X448PublicKeyParameters(byte[] byArray) {
        this(X448PublicKeyParameters.validate(byArray), 0);
    }

    public X448PublicKeyParameters(byte[] byArray, int n) {
        super(false);
        System.arraycopy(byArray, n, this.data, 0, 56);
    }

    public X448PublicKeyParameters(InputStream inputStream) throws IOException {
        super(false);
        if (56 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of X448 public key");
        }
    }

    public void encode(byte[] byArray, int n) {
        System.arraycopy(this.data, 0, byArray, n, 56);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    private static byte[] validate(byte[] byArray) {
        if (byArray.length != 56) {
            throw new IllegalArgumentException("'buf' must have length 56");
        }
        return byArray;
    }
}

