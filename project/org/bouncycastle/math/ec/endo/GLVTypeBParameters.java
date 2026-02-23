/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.math.ec.endo;

import java.math.BigInteger;
import org.bouncycastle.math.ec.endo.ScalarSplitParameters;

public class GLVTypeBParameters {
    protected final BigInteger beta;
    protected final BigInteger lambda;
    protected final ScalarSplitParameters splitParams;

    public GLVTypeBParameters(BigInteger bigInteger, BigInteger bigInteger2, BigInteger[] bigIntegerArray, BigInteger[] bigIntegerArray2, BigInteger bigInteger3, BigInteger bigInteger4, int n) {
        this.beta = bigInteger;
        this.lambda = bigInteger2;
        this.splitParams = new ScalarSplitParameters(bigIntegerArray, bigIntegerArray2, bigInteger3, bigInteger4, n);
    }

    public GLVTypeBParameters(BigInteger bigInteger, BigInteger bigInteger2, ScalarSplitParameters scalarSplitParameters) {
        this.beta = bigInteger;
        this.lambda = bigInteger2;
        this.splitParams = scalarSplitParameters;
    }

    public BigInteger getBeta() {
        return this.beta;
    }

    public BigInteger getLambda() {
        return this.lambda;
    }

    public ScalarSplitParameters getSplitParams() {
        return this.splitParams;
    }

    public BigInteger getV1A() {
        return this.getSplitParams().getV1A();
    }

    public BigInteger getV1B() {
        return this.getSplitParams().getV1B();
    }

    public BigInteger getV2A() {
        return this.getSplitParams().getV2A();
    }

    public BigInteger getV2B() {
        return this.getSplitParams().getV2B();
    }

    public BigInteger getG1() {
        return this.getSplitParams().getG1();
    }

    public BigInteger getG2() {
        return this.getSplitParams().getG2();
    }

    public int getBits() {
        return this.getSplitParams().getBits();
    }
}

