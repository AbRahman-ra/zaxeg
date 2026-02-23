/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed448;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class Ed448PrivateKeyParameters
extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 57;
    public static final int SIGNATURE_SIZE = 114;
    private final byte[] data = new byte[57];
    private Ed448PublicKeyParameters cachedPublicKey;

    public Ed448PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        Ed448.generatePrivateKey(secureRandom, this.data);
    }

    public Ed448PrivateKeyParameters(byte[] byArray) {
        this(Ed448PrivateKeyParameters.validate(byArray), 0);
    }

    public Ed448PrivateKeyParameters(byte[] byArray, int n) {
        super(true);
        System.arraycopy(byArray, n, this.data, 0, 57);
    }

    public Ed448PrivateKeyParameters(InputStream inputStream) throws IOException {
        super(true);
        if (57 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of Ed448 private key");
        }
    }

    public void encode(byte[] byArray, int n) {
        System.arraycopy(this.data, 0, byArray, n, 57);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Ed448PublicKeyParameters generatePublicKey() {
        byte[] byArray = this.data;
        synchronized (this.data) {
            if (null == this.cachedPublicKey) {
                byte[] byArray2 = new byte[57];
                Ed448.generatePublicKey(this.data, 0, byArray2, 0);
                this.cachedPublicKey = new Ed448PublicKeyParameters(byArray2, 0);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return this.cachedPublicKey;
        }
    }

    public void sign(int n, Ed448PublicKeyParameters ed448PublicKeyParameters, byte[] byArray, byte[] byArray2, int n2, int n3, byte[] byArray3, int n4) {
        this.sign(n, byArray, byArray2, n2, n3, byArray3, n4);
    }

    public void sign(int n, byte[] byArray, byte[] byArray2, int n2, int n3, byte[] byArray3, int n4) {
        Ed448PublicKeyParameters ed448PublicKeyParameters = this.generatePublicKey();
        byte[] byArray4 = new byte[57];
        ed448PublicKeyParameters.encode(byArray4, 0);
        switch (n) {
            case 0: {
                Ed448.sign(this.data, 0, byArray4, 0, byArray, byArray2, n2, n3, byArray3, n4);
                break;
            }
            case 1: {
                if (64 != n3) {
                    throw new IllegalArgumentException("msgLen");
                }
                Ed448.signPrehash(this.data, 0, byArray4, 0, byArray, byArray2, n2, byArray3, n4);
                break;
            }
            default: {
                throw new IllegalArgumentException("algorithm");
            }
        }
    }

    private static byte[] validate(byte[] byArray) {
        if (byArray.length != 57) {
            throw new IllegalArgumentException("'buf' must have length 57");
        }
        return byArray;
    }
}

