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

public final class Ed448PublicKeyParameters
extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 57;
    private final byte[] data = new byte[57];

    public Ed448PublicKeyParameters(byte[] byArray) {
        this(Ed448PublicKeyParameters.validate(byArray), 0);
    }

    public Ed448PublicKeyParameters(byte[] byArray, int n) {
        super(false);
        System.arraycopy(byArray, n, this.data, 0, 57);
    }

    public Ed448PublicKeyParameters(InputStream inputStream) throws IOException {
        super(false);
        if (57 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of Ed448 public key");
        }
    }

    public void encode(byte[] byArray, int n) {
        System.arraycopy(this.data, 0, byArray, n, 57);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    private static byte[] validate(byte[] byArray) {
        if (byArray.length != 57) {
            throw new IllegalArgumentException("'buf' must have length 57");
        }
        return byArray;
    }
}

