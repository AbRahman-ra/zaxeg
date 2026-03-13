/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.SM2KeyExchangePrivateParameters;
import org.bouncycastle.crypto.params.SM2KeyExchangePublicParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SM2KeyExchange {
    private final Digest digest;
    private byte[] userID;
    private ECPrivateKeyParameters staticKey;
    private ECPoint staticPubPoint;
    private ECPoint ephemeralPubPoint;
    private ECDomainParameters ecParams;
    private int w;
    private ECPrivateKeyParameters ephemeralKey;
    private boolean initiator;

    public SM2KeyExchange() {
        this(new SM3Digest());
    }

    public SM2KeyExchange(Digest digest) {
        this.digest = digest;
    }

    public void init(CipherParameters cipherParameters) {
        SM2KeyExchangePrivateParameters sM2KeyExchangePrivateParameters;
        if (cipherParameters instanceof ParametersWithID) {
            sM2KeyExchangePrivateParameters = (SM2KeyExchangePrivateParameters)((ParametersWithID)cipherParameters).getParameters();
            this.userID = ((ParametersWithID)cipherParameters).getID();
        } else {
            sM2KeyExchangePrivateParameters = (SM2KeyExchangePrivateParameters)cipherParameters;
            this.userID = new byte[0];
        }
        this.initiator = sM2KeyExchangePrivateParameters.isInitiator();
        this.staticKey = sM2KeyExchangePrivateParameters.getStaticPrivateKey();
        this.ephemeralKey = sM2KeyExchangePrivateParameters.getEphemeralPrivateKey();
        this.ecParams = this.staticKey.getParameters();
        this.staticPubPoint = sM2KeyExchangePrivateParameters.getStaticPublicPoint();
        this.ephemeralPubPoint = sM2KeyExchangePrivateParameters.getEphemeralPublicPoint();
        this.w = this.ecParams.getCurve().getFieldSize() / 2 - 1;
    }

    public byte[] calculateKey(int n, CipherParameters cipherParameters) {
        byte[] byArray;
        SM2KeyExchangePublicParameters sM2KeyExchangePublicParameters;
        if (cipherParameters instanceof ParametersWithID) {
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters)((ParametersWithID)cipherParameters).getParameters();
            byArray = ((ParametersWithID)cipherParameters).getID();
        } else {
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters)cipherParameters;
            byArray = new byte[]{};
        }
        byte[] byArray2 = this.getZ(this.digest, this.userID, this.staticPubPoint);
        byte[] byArray3 = this.getZ(this.digest, byArray, sM2KeyExchangePublicParameters.getStaticPublicKey().getQ());
        ECPoint eCPoint = this.calculateU(sM2KeyExchangePublicParameters);
        byte[] byArray4 = this.initiator ? this.kdf(eCPoint, byArray2, byArray3, n) : this.kdf(eCPoint, byArray3, byArray2, n);
        return byArray4;
    }

    public byte[][] calculateKeyWithConfirmation(int n, byte[] byArray, CipherParameters cipherParameters) {
        byte[] byArray2;
        SM2KeyExchangePublicParameters sM2KeyExchangePublicParameters;
        if (cipherParameters instanceof ParametersWithID) {
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters)((ParametersWithID)cipherParameters).getParameters();
            byArray2 = ((ParametersWithID)cipherParameters).getID();
        } else {
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters)cipherParameters;
            byArray2 = new byte[]{};
        }
        if (this.initiator && byArray == null) {
            throw new IllegalArgumentException("if initiating, confirmationTag must be set");
        }
        byte[] byArray3 = this.getZ(this.digest, this.userID, this.staticPubPoint);
        byte[] byArray4 = this.getZ(this.digest, byArray2, sM2KeyExchangePublicParameters.getStaticPublicKey().getQ());
        ECPoint eCPoint = this.calculateU(sM2KeyExchangePublicParameters);
        if (this.initiator) {
            byte[] byArray5 = this.kdf(eCPoint, byArray3, byArray4, n);
            byte[] byArray6 = this.calculateInnerHash(this.digest, eCPoint, byArray3, byArray4, this.ephemeralPubPoint, sM2KeyExchangePublicParameters.getEphemeralPublicKey().getQ());
            byte[] byArray7 = this.S1(this.digest, eCPoint, byArray6);
            if (!Arrays.constantTimeAreEqual(byArray7, byArray)) {
                throw new IllegalStateException("confirmation tag mismatch");
            }
            return new byte[][]{byArray5, this.S2(this.digest, eCPoint, byArray6)};
        }
        byte[] byArray8 = this.kdf(eCPoint, byArray4, byArray3, n);
        byte[] byArray9 = this.calculateInnerHash(this.digest, eCPoint, byArray4, byArray3, sM2KeyExchangePublicParameters.getEphemeralPublicKey().getQ(), this.ephemeralPubPoint);
        return new byte[][]{byArray8, this.S1(this.digest, eCPoint, byArray9), this.S2(this.digest, eCPoint, byArray9)};
    }

    private ECPoint calculateU(SM2KeyExchangePublicParameters sM2KeyExchangePublicParameters) {
        ECDomainParameters eCDomainParameters = this.staticKey.getParameters();
        ECPoint eCPoint = ECAlgorithms.cleanPoint(eCDomainParameters.getCurve(), sM2KeyExchangePublicParameters.getStaticPublicKey().getQ());
        ECPoint eCPoint2 = ECAlgorithms.cleanPoint(eCDomainParameters.getCurve(), sM2KeyExchangePublicParameters.getEphemeralPublicKey().getQ());
        BigInteger bigInteger = this.reduce(this.ephemeralPubPoint.getAffineXCoord().toBigInteger());
        BigInteger bigInteger2 = this.reduce(eCPoint2.getAffineXCoord().toBigInteger());
        BigInteger bigInteger3 = this.staticKey.getD().add(bigInteger.multiply(this.ephemeralKey.getD()));
        BigInteger bigInteger4 = this.ecParams.getH().multiply(bigInteger3).mod(this.ecParams.getN());
        BigInteger bigInteger5 = bigInteger4.multiply(bigInteger2).mod(this.ecParams.getN());
        return ECAlgorithms.sumOfTwoMultiplies(eCPoint, bigInteger4, eCPoint2, bigInteger5).normalize();
    }

    private byte[] kdf(ECPoint eCPoint, byte[] byArray, byte[] byArray2, int n) {
        int n2 = this.digest.getDigestSize();
        byte[] byArray3 = new byte[Math.max(4, n2)];
        byte[] byArray4 = new byte[(n + 7) / 8];
        int n3 = 0;
        Memoable memoable = null;
        Memoable memoable2 = null;
        if (this.digest instanceof Memoable) {
            this.addFieldElement(this.digest, eCPoint.getAffineXCoord());
            this.addFieldElement(this.digest, eCPoint.getAffineYCoord());
            this.digest.update(byArray, 0, byArray.length);
            this.digest.update(byArray2, 0, byArray2.length);
            memoable = (Memoable)((Object)this.digest);
            memoable2 = memoable.copy();
        }
        int n4 = 0;
        while (n3 < byArray4.length) {
            if (memoable != null) {
                memoable.reset(memoable2);
            } else {
                this.addFieldElement(this.digest, eCPoint.getAffineXCoord());
                this.addFieldElement(this.digest, eCPoint.getAffineYCoord());
                this.digest.update(byArray, 0, byArray.length);
                this.digest.update(byArray2, 0, byArray2.length);
            }
            Pack.intToBigEndian(++n4, byArray3, 0);
            this.digest.update(byArray3, 0, 4);
            this.digest.doFinal(byArray3, 0);
            int n5 = Math.min(n2, byArray4.length - n3);
            System.arraycopy(byArray3, 0, byArray4, n3, n5);
            n3 += n5;
        }
        return byArray4;
    }

    private BigInteger reduce(BigInteger bigInteger) {
        return bigInteger.and(BigInteger.valueOf(1L).shiftLeft(this.w).subtract(BigInteger.valueOf(1L))).setBit(this.w);
    }

    private byte[] S1(Digest digest, ECPoint eCPoint, byte[] byArray) {
        digest.update((byte)2);
        this.addFieldElement(digest, eCPoint.getAffineYCoord());
        digest.update(byArray, 0, byArray.length);
        return this.digestDoFinal();
    }

    private byte[] calculateInnerHash(Digest digest, ECPoint eCPoint, byte[] byArray, byte[] byArray2, ECPoint eCPoint2, ECPoint eCPoint3) {
        this.addFieldElement(digest, eCPoint.getAffineXCoord());
        digest.update(byArray, 0, byArray.length);
        digest.update(byArray2, 0, byArray2.length);
        this.addFieldElement(digest, eCPoint2.getAffineXCoord());
        this.addFieldElement(digest, eCPoint2.getAffineYCoord());
        this.addFieldElement(digest, eCPoint3.getAffineXCoord());
        this.addFieldElement(digest, eCPoint3.getAffineYCoord());
        return this.digestDoFinal();
    }

    private byte[] S2(Digest digest, ECPoint eCPoint, byte[] byArray) {
        digest.update((byte)3);
        this.addFieldElement(digest, eCPoint.getAffineYCoord());
        digest.update(byArray, 0, byArray.length);
        return this.digestDoFinal();
    }

    private byte[] getZ(Digest digest, byte[] byArray, ECPoint eCPoint) {
        this.addUserID(digest, byArray);
        this.addFieldElement(digest, this.ecParams.getCurve().getA());
        this.addFieldElement(digest, this.ecParams.getCurve().getB());
        this.addFieldElement(digest, this.ecParams.getG().getAffineXCoord());
        this.addFieldElement(digest, this.ecParams.getG().getAffineYCoord());
        this.addFieldElement(digest, eCPoint.getAffineXCoord());
        this.addFieldElement(digest, eCPoint.getAffineYCoord());
        return this.digestDoFinal();
    }

    private void addUserID(Digest digest, byte[] byArray) {
        int n = byArray.length * 8;
        digest.update((byte)(n >>> 8));
        digest.update((byte)n);
        digest.update(byArray, 0, byArray.length);
    }

    private void addFieldElement(Digest digest, ECFieldElement eCFieldElement) {
        byte[] byArray = eCFieldElement.getEncoded();
        digest.update(byArray, 0, byArray.length);
    }

    private byte[] digestDoFinal() {
        byte[] byArray = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(byArray, 0);
        return byArray;
    }
}

