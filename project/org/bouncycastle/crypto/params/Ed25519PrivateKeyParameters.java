/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public final class Ed25519PrivateKeyParameters
extends AsymmetricKeyParameter {
    public static final int KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private final byte[] data = new byte[32];
    private Ed25519PublicKeyParameters cachedPublicKey;

    public Ed25519PrivateKeyParameters(SecureRandom secureRandom) {
        super(true);
        Ed25519.generatePrivateKey(secureRandom, this.data);
    }

    public Ed25519PrivateKeyParameters(byte[] byArray) {
        this(Ed25519PrivateKeyParameters.validate(byArray), 0);
    }

    public Ed25519PrivateKeyParameters(byte[] byArray, int n) {
        super(true);
        System.arraycopy(byArray, n, this.data, 0, 32);
    }

    public Ed25519PrivateKeyParameters(InputStream inputStream) throws IOException {
        super(true);
        if (32 != Streams.readFully(inputStream, this.data)) {
            throw new EOFException("EOF encountered in middle of Ed25519 private key");
        }
    }

    public void encode(byte[] byArray, int n) {
        System.arraycopy(this.data, 0, byArray, n, 32);
    }

    public byte[] getEncoded() {
        return Arrays.clone(this.data);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Ed25519PublicKeyParameters generatePublicKey() {
        byte[] byArray = this.data;
        synchronized (this.data) {
            if (null == this.cachedPublicKey) {
                byte[] byArray2 = new byte[32];
                Ed25519.generatePublicKey(this.data, 0, byArray2, 0);
                this.cachedPublicKey = new Ed25519PublicKeyParameters(byArray2, 0);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return this.cachedPublicKey;
        }
    }

    public void sign(int n, Ed25519PublicKeyParameters ed25519PublicKeyParameters, byte[] byArray, byte[] byArray2, int n2, int n3, byte[] byArray3, int n4) {
        this.sign(n, byArray, byArray2, n2, n3, byArray3, n4);
    }

    public void sign(int n, byte[] byArray, byte[] byArray2, int n2, int n3, byte[] byArray3, int n4) {
        Ed25519PublicKeyParameters ed25519PublicKeyParameters = this.generatePublicKey();
        byte[] byArray4 = new byte[32];
        ed25519PublicKeyParameters.encode(byArray4, 0);
        switch (n) {
            case 0: {
                if (null != byArray) {
                    throw new IllegalArgumentException("ctx");
                }
                Ed25519.sign(this.data, 0, byArray4, 0, byArray2, n2, n3, byArray3, n4);
                break;
            }
            case 1: {
                Ed25519.sign(this.data, 0, byArray4, 0, byArray, byArray2, n2, n3, byArray3, n4);
                break;
            }
            case 2: {
                if (64 != n3) {
                    throw new IllegalArgumentException("msgLen");
                }
                Ed25519.signPrehash(this.data, 0, byArray4, 0, byArray, byArray2, n2, byArray3, n4);
                break;
            }
            default: {
                throw new IllegalArgumentException("algorithm");
            }
        }
    }

    private static byte[] validate(byte[] byArray) {
        if (byArray.length != 32) {
            throw new IllegalArgumentException("'buf' must have length 32");
        }
        return byArray;
    }
}

