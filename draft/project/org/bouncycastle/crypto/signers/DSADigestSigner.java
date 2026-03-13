/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.DSAExt;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.DSAEncoding;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;

public class DSADigestSigner
implements Signer {
    private final DSA dsa;
    private final Digest digest;
    private final DSAEncoding encoding;
    private boolean forSigning;

    public DSADigestSigner(DSA dSA, Digest digest) {
        this.dsa = dSA;
        this.digest = digest;
        this.encoding = StandardDSAEncoding.INSTANCE;
    }

    public DSADigestSigner(DSAExt dSAExt, Digest digest, DSAEncoding dSAEncoding) {
        this.dsa = dSAExt;
        this.digest = digest;
        this.encoding = dSAEncoding;
    }

    public void init(boolean bl, CipherParameters cipherParameters) {
        this.forSigning = bl;
        AsymmetricKeyParameter asymmetricKeyParameter = cipherParameters instanceof ParametersWithRandom ? (AsymmetricKeyParameter)((ParametersWithRandom)cipherParameters).getParameters() : (AsymmetricKeyParameter)cipherParameters;
        if (bl && !asymmetricKeyParameter.isPrivate()) {
            throw new IllegalArgumentException("Signing Requires Private Key.");
        }
        if (!bl && asymmetricKeyParameter.isPrivate()) {
            throw new IllegalArgumentException("Verification Requires Public Key.");
        }
        this.reset();
        this.dsa.init(bl, cipherParameters);
    }

    public void update(byte by) {
        this.digest.update(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.digest.update(byArray, n, n2);
    }

    public byte[] generateSignature() {
        if (!this.forSigning) {
            throw new IllegalStateException("DSADigestSigner not initialised for signature generation.");
        }
        byte[] byArray = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(byArray, 0);
        BigInteger[] bigIntegerArray = this.dsa.generateSignature(byArray);
        try {
            return this.encoding.encode(this.getOrder(), bigIntegerArray[0], bigIntegerArray[1]);
        } catch (Exception exception) {
            throw new IllegalStateException("unable to encode signature");
        }
    }

    public boolean verifySignature(byte[] byArray) {
        if (this.forSigning) {
            throw new IllegalStateException("DSADigestSigner not initialised for verification");
        }
        byte[] byArray2 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(byArray2, 0);
        try {
            BigInteger[] bigIntegerArray = this.encoding.decode(this.getOrder(), byArray);
            return this.dsa.verifySignature(byArray2, bigIntegerArray[0], bigIntegerArray[1]);
        } catch (Exception exception) {
            return false;
        }
    }

    public void reset() {
        this.digest.reset();
    }

    protected BigInteger getOrder() {
        return this.dsa instanceof DSAExt ? ((DSAExt)this.dsa).getOrder() : null;
    }
}

