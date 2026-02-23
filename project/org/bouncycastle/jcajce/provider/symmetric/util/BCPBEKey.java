/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.symmetric.util;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Destroyable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class BCPBEKey
implements PBEKey,
Destroyable {
    private final AtomicBoolean hasBeenDestroyed = new AtomicBoolean(false);
    String algorithm;
    ASN1ObjectIdentifier oid;
    int type;
    int digest;
    int keySize;
    int ivSize;
    private final char[] password;
    private final byte[] salt;
    private final int iterationCount;
    private final CipherParameters param;
    boolean tryWrong = false;

    public BCPBEKey(String string, ASN1ObjectIdentifier aSN1ObjectIdentifier, int n, int n2, int n3, int n4, PBEKeySpec pBEKeySpec, CipherParameters cipherParameters) {
        this.algorithm = string;
        this.oid = aSN1ObjectIdentifier;
        this.type = n;
        this.digest = n2;
        this.keySize = n3;
        this.ivSize = n4;
        this.password = pBEKeySpec.getPassword();
        this.iterationCount = pBEKeySpec.getIterationCount();
        this.salt = pBEKeySpec.getSalt();
        this.param = cipherParameters;
    }

    public BCPBEKey(String string, CipherParameters cipherParameters) {
        this.algorithm = string;
        this.param = cipherParameters;
        this.password = null;
        this.iterationCount = -1;
        this.salt = null;
    }

    public String getAlgorithm() {
        BCPBEKey.checkDestroyed(this);
        return this.algorithm;
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        BCPBEKey.checkDestroyed(this);
        if (this.param != null) {
            KeyParameter keyParameter = this.param instanceof ParametersWithIV ? (KeyParameter)((ParametersWithIV)this.param).getParameters() : (KeyParameter)this.param;
            return keyParameter.getKey();
        }
        if (this.type == 2) {
            return PBEParametersGenerator.PKCS12PasswordToBytes(this.password);
        }
        if (this.type == 5) {
            return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(this.password);
        }
        return PBEParametersGenerator.PKCS5PasswordToBytes(this.password);
    }

    int getType() {
        BCPBEKey.checkDestroyed(this);
        return this.type;
    }

    int getDigest() {
        BCPBEKey.checkDestroyed(this);
        return this.digest;
    }

    int getKeySize() {
        BCPBEKey.checkDestroyed(this);
        return this.keySize;
    }

    public int getIvSize() {
        BCPBEKey.checkDestroyed(this);
        return this.ivSize;
    }

    public CipherParameters getParam() {
        BCPBEKey.checkDestroyed(this);
        return this.param;
    }

    public char[] getPassword() {
        BCPBEKey.checkDestroyed(this);
        if (this.password == null) {
            throw new IllegalStateException("no password available");
        }
        return Arrays.clone(this.password);
    }

    public byte[] getSalt() {
        BCPBEKey.checkDestroyed(this);
        return Arrays.clone(this.salt);
    }

    public int getIterationCount() {
        BCPBEKey.checkDestroyed(this);
        return this.iterationCount;
    }

    public ASN1ObjectIdentifier getOID() {
        BCPBEKey.checkDestroyed(this);
        return this.oid;
    }

    public void setTryWrongPKCS12Zero(boolean bl) {
        this.tryWrong = bl;
    }

    boolean shouldTryWrongPKCS12() {
        return this.tryWrong;
    }

    public void destroy() {
        if (!this.hasBeenDestroyed.getAndSet(true)) {
            if (this.password != null) {
                Arrays.fill(this.password, '\u0000');
            }
            if (this.salt != null) {
                Arrays.fill(this.salt, (byte)0);
            }
        }
    }

    public boolean isDestroyed() {
        return this.hasBeenDestroyed.get();
    }

    static void checkDestroyed(Destroyable destroyable) {
        if (destroyable.isDestroyed()) {
            throw new IllegalStateException("key has been destroyed");
        }
    }
}

