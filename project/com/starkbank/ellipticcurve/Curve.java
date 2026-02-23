/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve;

import com.starkbank.ellipticcurve.Point;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Curve {
    public BigInteger A;
    public BigInteger B;
    public BigInteger P;
    public BigInteger N;
    public Point G;
    public String name;
    public long[] oid;
    public static final Curve secp256k1 = new Curve(BigInteger.ZERO, BigInteger.valueOf(7L), new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16), new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16), new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16), new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16), "secp256k1", new long[]{1L, 3L, 132L, 0L, 10L});
    public static final List supportedCurves = new ArrayList();
    public static final Map curvesByOid = new HashMap();

    public Curve(BigInteger A, BigInteger B, BigInteger P, BigInteger N, BigInteger Gx, BigInteger Gy, String name, long[] oid) {
        this.A = A;
        this.B = B;
        this.P = P;
        this.N = N;
        this.G = new Point(Gx, Gy);
        this.name = name;
        this.oid = oid;
    }

    public boolean contains(Point p) {
        if (p.x.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        if (p.x.compareTo(this.P) >= 0) {
            return false;
        }
        if (p.y.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        if (p.y.compareTo(this.P) >= 0) {
            return false;
        }
        return p.y.pow(2).subtract(p.x.pow(3).add(this.A.multiply(p.x)).add(this.B)).mod(this.P).intValue() == 0;
    }

    public int length() {
        return (1 + this.N.toString(16).length()) / 2;
    }

    static {
        supportedCurves.add(secp256k1);
        for (Object c : supportedCurves) {
            Curve curve = (Curve)c;
            curvesByOid.put(Arrays.hashCode(curve.oid), curve);
        }
    }
}

