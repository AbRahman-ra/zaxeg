/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve;

import com.starkbank.ellipticcurve.Point;
import java.math.BigInteger;

public final class Math {
    public static Point multiply(Point p, BigInteger n, BigInteger N, BigInteger A, BigInteger P) {
        return Math.fromJacobian(Math.jacobianMultiply(Math.toJacobian(p), n, N, A, P), P);
    }

    public static Point add(Point p, Point q, BigInteger A, BigInteger P) {
        return Math.fromJacobian(Math.jacobianAdd(Math.toJacobian(p), Math.toJacobian(q), A, P), P);
    }

    public static BigInteger inv(BigInteger x, BigInteger n) {
        if (x.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO;
        }
        BigInteger lm = BigInteger.ONE;
        BigInteger hm = BigInteger.ZERO;
        BigInteger high = n;
        BigInteger low = x.mod(n);
        while (low.compareTo(BigInteger.ONE) > 0) {
            BigInteger r = high.divide(low);
            BigInteger nm = hm.subtract(lm.multiply(r));
            BigInteger nw = high.subtract(low.multiply(r));
            high = low;
            hm = lm;
            low = nw;
            lm = nm;
        }
        return lm.mod(n);
    }

    public static Point toJacobian(Point p) {
        return new Point(p.x, p.y, BigInteger.ONE);
    }

    public static Point fromJacobian(Point p, BigInteger P) {
        BigInteger z = Math.inv(p.z, P);
        BigInteger x = p.x.multiply(z.pow(2)).mod(P);
        BigInteger y = p.y.multiply(z.pow(3)).mod(P);
        return new Point(x, y, BigInteger.ZERO);
    }

    public static Point jacobianDouble(Point p, BigInteger A, BigInteger P) {
        if (p.y == null || p.y.equals(BigInteger.ZERO)) {
            return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        }
        BigInteger ysq = p.y.pow(2).mod(P);
        BigInteger S = BigInteger.valueOf(4L).multiply(p.x).multiply(ysq).mod(P);
        BigInteger M2 = BigInteger.valueOf(3L).multiply(p.x.pow(2)).add(A.multiply(p.z.pow(4))).mod(P);
        BigInteger nx = M2.pow(2).subtract(BigInteger.valueOf(2L).multiply(S)).mod(P);
        BigInteger ny = M2.multiply(S.subtract(nx)).subtract(BigInteger.valueOf(8L).multiply(ysq.pow(2))).mod(P);
        BigInteger nz = BigInteger.valueOf(2L).multiply(p.y).multiply(p.z).mod(P);
        return new Point(nx, ny, nz);
    }

    public static Point jacobianAdd(Point p, Point q, BigInteger A, BigInteger P) {
        if (p.y == null || p.y.equals(BigInteger.ZERO)) {
            return q;
        }
        if (q.y == null || q.y.equals(BigInteger.ZERO)) {
            return p;
        }
        BigInteger U1 = p.x.multiply(q.z.pow(2)).mod(P);
        BigInteger U2 = q.x.multiply(p.z.pow(2)).mod(P);
        BigInteger S1 = p.y.multiply(q.z.pow(3)).mod(P);
        BigInteger S2 = q.y.multiply(p.z.pow(3)).mod(P);
        if (U1.compareTo(U2) == 0) {
            if (S1.compareTo(S2) != 0) {
                return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
            }
            return Math.jacobianDouble(p, A, P);
        }
        BigInteger H = U2.subtract(U1);
        BigInteger R = S2.subtract(S1);
        BigInteger H2 = H.multiply(H).mod(P);
        BigInteger H3 = H.multiply(H2).mod(P);
        BigInteger U1H2 = U1.multiply(H2).mod(P);
        BigInteger nx = R.pow(2).subtract(H3).subtract(BigInteger.valueOf(2L).multiply(U1H2)).mod(P);
        BigInteger ny = R.multiply(U1H2.subtract(nx)).subtract(S1.multiply(H3)).mod(P);
        BigInteger nz = H.multiply(p.z).multiply(q.z).mod(P);
        return new Point(nx, ny, nz);
    }

    public static Point jacobianMultiply(Point p, BigInteger n, BigInteger N, BigInteger A, BigInteger P) {
        if (BigInteger.ZERO.compareTo(p.y) == 0 || BigInteger.ZERO.compareTo(n) == 0) {
            return new Point(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
        }
        if (BigInteger.ONE.compareTo(n) == 0) {
            return p;
        }
        if (n.compareTo(BigInteger.ZERO) < 0 || n.compareTo(N) >= 0) {
            return Math.jacobianMultiply(p, n.mod(N), N, A, P);
        }
        if (n.mod(BigInteger.valueOf(2L)).compareTo(BigInteger.ZERO) == 0) {
            return Math.jacobianDouble(Math.jacobianMultiply(p, n.divide(BigInteger.valueOf(2L)), N, A, P), A, P);
        }
        if (n.mod(BigInteger.valueOf(2L)).compareTo(BigInteger.ONE) == 0) {
            return Math.jacobianAdd(Math.jacobianDouble(Math.jacobianMultiply(p, n.divide(BigInteger.valueOf(2L)), N, A, P), A, P), p, A, P);
        }
        return null;
    }
}

