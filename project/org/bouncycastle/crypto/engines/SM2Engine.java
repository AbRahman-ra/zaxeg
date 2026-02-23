/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.engines;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SM2Engine {
    private final Digest digest;
    private final Mode mode;
    private boolean forEncryption;
    private ECKeyParameters ecKey;
    private ECDomainParameters ecParams;
    private int curveLength;
    private SecureRandom random;

    public SM2Engine() {
        this(new SM3Digest());
    }

    public SM2Engine(Mode mode) {
        this(new SM3Digest(), mode);
    }

    public SM2Engine(Digest digest) {
        this(digest, Mode.C1C2C3);
    }

    public SM2Engine(Digest digest, Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode cannot be NULL");
        }
        this.digest = digest;
        this.mode = mode;
    }

    public void init(boolean bl, CipherParameters cipherParameters) {
        this.forEncryption = bl;
        if (bl) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom)cipherParameters;
            this.ecKey = (ECKeyParameters)parametersWithRandom.getParameters();
            this.ecParams = this.ecKey.getParameters();
            ECPoint eCPoint = ((ECPublicKeyParameters)this.ecKey).getQ().multiply(this.ecParams.getH());
            if (eCPoint.isInfinity()) {
                throw new IllegalArgumentException("invalid key: [h]Q at infinity");
            }
            this.random = parametersWithRandom.getRandom();
        } else {
            this.ecKey = (ECKeyParameters)cipherParameters;
            this.ecParams = this.ecKey.getParameters();
        }
        this.curveLength = (this.ecParams.getCurve().getFieldSize() + 7) / 8;
    }

    public byte[] processBlock(byte[] byArray, int n, int n2) throws InvalidCipherTextException {
        if (this.forEncryption) {
            return this.encrypt(byArray, n, n2);
        }
        return this.decrypt(byArray, n, n2);
    }

    public int getOutputSize(int n) {
        return 1 + 2 * this.curveLength + n + this.digest.getDigestSize();
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    private byte[] encrypt(byte[] byArray, int n, int n2) throws InvalidCipherTextException {
        ECPoint eCPoint;
        byte[] byArray2;
        Object object;
        byte[] byArray3 = new byte[n2];
        System.arraycopy(byArray, n, byArray3, 0, byArray3.length);
        ECMultiplier eCMultiplier = this.createBasePointMultiplier();
        do {
            object = this.nextK();
            ECPoint eCPoint2 = eCMultiplier.multiply(this.ecParams.getG(), (BigInteger)object).normalize();
            byArray2 = eCPoint2.getEncoded(false);
            eCPoint = ((ECPublicKeyParameters)this.ecKey).getQ().multiply((BigInteger)object).normalize();
            this.kdf(this.digest, eCPoint, byArray3);
        } while (this.notEncrypted(byArray3, byArray, n));
        object = new byte[this.digest.getDigestSize()];
        this.addFieldElement(this.digest, eCPoint.getAffineXCoord());
        this.digest.update(byArray, n, n2);
        this.addFieldElement(this.digest, eCPoint.getAffineYCoord());
        this.digest.doFinal((byte[])object, 0);
        switch (this.mode) {
            case C1C3C2: {
                return Arrays.concatenate(byArray2, (byte[])object, byArray3);
            }
        }
        return Arrays.concatenate(byArray2, byArray3, (byte[])object);
    }

    private byte[] decrypt(byte[] byArray, int n, int n2) throws InvalidCipherTextException {
        byte[] byArray2 = new byte[this.curveLength * 2 + 1];
        System.arraycopy(byArray, n, byArray2, 0, byArray2.length);
        ECPoint eCPoint = this.ecParams.getCurve().decodePoint(byArray2);
        ECPoint eCPoint2 = eCPoint.multiply(this.ecParams.getH());
        if (eCPoint2.isInfinity()) {
            throw new InvalidCipherTextException("[h]C1 at infinity");
        }
        eCPoint = eCPoint.multiply(((ECPrivateKeyParameters)this.ecKey).getD()).normalize();
        int n3 = this.digest.getDigestSize();
        byte[] byArray3 = new byte[n2 - byArray2.length - n3];
        if (this.mode == Mode.C1C3C2) {
            System.arraycopy(byArray, n + byArray2.length + n3, byArray3, 0, byArray3.length);
        } else {
            System.arraycopy(byArray, n + byArray2.length, byArray3, 0, byArray3.length);
        }
        this.kdf(this.digest, eCPoint, byArray3);
        byte[] byArray4 = new byte[this.digest.getDigestSize()];
        this.addFieldElement(this.digest, eCPoint.getAffineXCoord());
        this.digest.update(byArray3, 0, byArray3.length);
        this.addFieldElement(this.digest, eCPoint.getAffineYCoord());
        this.digest.doFinal(byArray4, 0);
        int n4 = 0;
        if (this.mode == Mode.C1C3C2) {
            for (int i = 0; i != byArray4.length; ++i) {
                n4 |= byArray4[i] ^ byArray[n + byArray2.length + i];
            }
        } else {
            for (int i = 0; i != byArray4.length; ++i) {
                n4 |= byArray4[i] ^ byArray[n + byArray2.length + byArray3.length + i];
            }
        }
        Arrays.fill(byArray2, (byte)0);
        Arrays.fill(byArray4, (byte)0);
        if (n4 != 0) {
            Arrays.fill(byArray3, (byte)0);
            throw new InvalidCipherTextException("invalid cipher text");
        }
        return byArray3;
    }

    private boolean notEncrypted(byte[] byArray, byte[] byArray2, int n) {
        for (int i = 0; i != byArray.length; ++i) {
            if (byArray[i] == byArray2[n + i]) continue;
            return false;
        }
        return true;
    }

    private void kdf(Digest digest, ECPoint eCPoint, byte[] byArray) {
        int n = digest.getDigestSize();
        byte[] byArray2 = new byte[Math.max(4, n)];
        int n2 = 0;
        Memoable memoable = null;
        Memoable memoable2 = null;
        if (digest instanceof Memoable) {
            this.addFieldElement(digest, eCPoint.getAffineXCoord());
            this.addFieldElement(digest, eCPoint.getAffineYCoord());
            memoable = (Memoable)((Object)digest);
            memoable2 = memoable.copy();
        }
        int n3 = 0;
        while (n2 < byArray.length) {
            if (memoable != null) {
                memoable.reset(memoable2);
            } else {
                this.addFieldElement(digest, eCPoint.getAffineXCoord());
                this.addFieldElement(digest, eCPoint.getAffineYCoord());
            }
            Pack.intToBigEndian(++n3, byArray2, 0);
            digest.update(byArray2, 0, 4);
            digest.doFinal(byArray2, 0);
            int n4 = Math.min(n, byArray.length - n2);
            this.xor(byArray, byArray2, n2, n4);
            n2 += n4;
        }
    }

    private void xor(byte[] byArray, byte[] byArray2, int n, int n2) {
        for (int i = 0; i != n2; ++i) {
            int n3 = n + i;
            byArray[n3] = (byte)(byArray[n3] ^ byArray2[i]);
        }
    }

    private BigInteger nextK() {
        BigInteger bigInteger;
        int n = this.ecParams.getN().bitLength();
        while ((bigInteger = BigIntegers.createRandomBigInteger(n, this.random)).equals(BigIntegers.ZERO) || bigInteger.compareTo(this.ecParams.getN()) >= 0) {
        }
        return bigInteger;
    }

    private void addFieldElement(Digest digest, ECFieldElement eCFieldElement) {
        byte[] byArray = BigIntegers.asUnsignedByteArray(this.curveLength, eCFieldElement.toBigInteger());
        digest.update(byArray, 0, byArray.length);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Mode {
        C1C2C3,
        C1C3C2;

    }
}

