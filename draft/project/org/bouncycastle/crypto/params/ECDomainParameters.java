/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.params;

import java.math.BigInteger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

public class ECDomainParameters
implements ECConstants {
    private final ECCurve curve;
    private final byte[] seed;
    private final ECPoint G;
    private final BigInteger n;
    private final BigInteger h;
    private BigInteger hInv = null;

    public ECDomainParameters(X9ECParameters x9ECParameters) {
        this(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN(), x9ECParameters.getH(), x9ECParameters.getSeed());
    }

    public ECDomainParameters(ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger) {
        this(eCCurve, eCPoint, bigInteger, ONE, null);
    }

    public ECDomainParameters(ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2) {
        this(eCCurve, eCPoint, bigInteger, bigInteger2, null);
    }

    public ECDomainParameters(ECCurve eCCurve, ECPoint eCPoint, BigInteger bigInteger, BigInteger bigInteger2, byte[] byArray) {
        if (eCCurve == null) {
            throw new NullPointerException("curve");
        }
        if (bigInteger == null) {
            throw new NullPointerException("n");
        }
        this.curve = eCCurve;
        this.G = ECDomainParameters.validatePublicPoint(eCCurve, eCPoint);
        this.n = bigInteger;
        this.h = bigInteger2;
        this.seed = Arrays.clone(byArray);
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    public ECPoint getG() {
        return this.G;
    }

    public BigInteger getN() {
        return this.n;
    }

    public BigInteger getH() {
        return this.h;
    }

    public synchronized BigInteger getHInv() {
        if (this.hInv == null) {
            this.hInv = BigIntegers.modOddInverseVar(this.n, this.h);
        }
        return this.hInv;
    }

    public byte[] getSeed() {
        return Arrays.clone(this.seed);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ECDomainParameters)) {
            return false;
        }
        ECDomainParameters eCDomainParameters = (ECDomainParameters)object;
        return this.curve.equals(eCDomainParameters.curve) && this.G.equals(eCDomainParameters.G) && this.n.equals(eCDomainParameters.n);
    }

    public int hashCode() {
        int n = 4;
        n *= 257;
        n ^= this.curve.hashCode();
        n *= 257;
        n ^= this.G.hashCode();
        n *= 257;
        return n ^= this.n.hashCode();
    }

    public BigInteger validatePrivateScalar(BigInteger bigInteger) {
        if (null == bigInteger) {
            throw new NullPointerException("Scalar cannot be null");
        }
        if (bigInteger.compareTo(ECConstants.ONE) < 0 || bigInteger.compareTo(this.getN()) >= 0) {
            throw new IllegalArgumentException("Scalar is not in the interval [1, n - 1]");
        }
        return bigInteger;
    }

    public ECPoint validatePublicPoint(ECPoint eCPoint) {
        return ECDomainParameters.validatePublicPoint(this.getCurve(), eCPoint);
    }

    static ECPoint validatePublicPoint(ECCurve eCCurve, ECPoint eCPoint) {
        if (null == eCPoint) {
            throw new NullPointerException("Point cannot be null");
        }
        if ((eCPoint = ECAlgorithms.importPoint(eCCurve, eCPoint).normalize()).isInfinity()) {
            throw new IllegalArgumentException("Point at infinity");
        }
        if (!eCPoint.isValid()) {
            throw new IllegalArgumentException("Point not on curve");
        }
        return eCPoint;
    }
}

