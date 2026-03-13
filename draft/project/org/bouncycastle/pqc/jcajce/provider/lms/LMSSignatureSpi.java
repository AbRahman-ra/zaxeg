/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.jcajce.provider.lms;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.NullDigest;
import org.bouncycastle.pqc.crypto.ExhaustedPrivateKeyException;
import org.bouncycastle.pqc.crypto.MessageSigner;
import org.bouncycastle.pqc.crypto.lms.LMSContext;
import org.bouncycastle.pqc.crypto.lms.LMSContextBasedSigner;
import org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier;
import org.bouncycastle.pqc.jcajce.provider.lms.BCLMSPrivateKey;
import org.bouncycastle.pqc.jcajce.provider.lms.BCLMSPublicKey;
import org.bouncycastle.pqc.jcajce.provider.lms.DigestUtil;

public class LMSSignatureSpi
extends Signature {
    private Digest digest;
    private MessageSigner signer;
    private SecureRandom random;
    private LMSContextBasedSigner lmOtsSigner;
    private LMSContextBasedVerifier lmOtsVerifier;

    protected LMSSignatureSpi(String string) {
        super(string);
    }

    protected LMSSignatureSpi(String string, Digest digest) {
        super(string);
        this.digest = digest;
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (!(publicKey instanceof BCLMSPublicKey)) {
            throw new InvalidKeyException("unknown public key passed to XMSS");
        }
        this.digest = new NullDigest();
        this.digest.reset();
        this.lmOtsVerifier = (LMSContextBasedVerifier)((Object)((BCLMSPublicKey)publicKey).getKeyParams());
    }

    protected void engineInitSign(PrivateKey privateKey, SecureRandom secureRandom) throws InvalidKeyException {
        this.random = secureRandom;
        this.engineInitSign(privateKey);
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (privateKey instanceof BCLMSPrivateKey) {
            this.lmOtsSigner = (LMSContextBasedSigner)((Object)((BCLMSPrivateKey)privateKey).getKeyParams());
            if (this.lmOtsSigner.getUsagesRemaining() == 0L) {
                throw new InvalidKeyException("private key exhausted");
            }
        } else {
            throw new InvalidKeyException("unknown private key passed to LMS");
        }
        this.digest = null;
    }

    protected void engineUpdate(byte by) throws SignatureException {
        if (this.digest == null) {
            this.digest = this.getSigner();
        }
        this.digest.update(by);
    }

    protected void engineUpdate(byte[] byArray, int n, int n2) throws SignatureException {
        if (this.digest == null) {
            this.digest = this.getSigner();
        }
        this.digest.update(byArray, n, n2);
    }

    private Digest getSigner() throws SignatureException {
        try {
            return this.lmOtsSigner.generateLMSContext();
        } catch (ExhaustedPrivateKeyException exhaustedPrivateKeyException) {
            throw new SignatureException(exhaustedPrivateKeyException.getMessage(), exhaustedPrivateKeyException);
        }
    }

    protected byte[] engineSign() throws SignatureException {
        if (this.digest == null) {
            this.digest = this.getSigner();
        }
        try {
            byte[] byArray = this.lmOtsSigner.generateSignature((LMSContext)this.digest);
            this.digest = null;
            return byArray;
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException) {
                throw new SignatureException(exception.getMessage(), exception);
            }
            throw new SignatureException(exception.toString(), exception);
        }
    }

    protected boolean engineVerify(byte[] byArray) throws SignatureException {
        LMSContext lMSContext = this.lmOtsVerifier.generateLMSContext(byArray);
        byte[] byArray2 = DigestUtil.getDigestResult(this.digest);
        lMSContext.update(byArray2, 0, byArray2.length);
        return this.lmOtsVerifier.verify(lMSContext);
    }

    protected void engineSetParameter(AlgorithmParameterSpec algorithmParameterSpec) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    protected void engineSetParameter(String string, Object object) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    protected Object engineGetParameter(String string) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    public static class generic
    extends LMSSignatureSpi {
        public generic() {
            super("LMS", new NullDigest());
        }
    }
}

