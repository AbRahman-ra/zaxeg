/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve;

import com.starkbank.ellipticcurve.Curve;
import com.starkbank.ellipticcurve.Math;
import com.starkbank.ellipticcurve.Point;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.BinaryAscii;
import com.starkbank.ellipticcurve.utils.RandomInteger;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Ecdsa {
    public static Signature sign(String message, PrivateKey privateKey, MessageDigest hashfunc) {
        byte[] hashMessage = hashfunc.digest(message.getBytes());
        BigInteger numberMessage = BinaryAscii.numberFromString(hashMessage);
        Curve curve = privateKey.curve;
        BigInteger randNum = RandomInteger.between(BigInteger.ONE, curve.N);
        Point randomSignPoint = Math.multiply(curve.G, randNum, curve.N, curve.A, curve.P);
        BigInteger r = randomSignPoint.x.mod(curve.N);
        BigInteger s = numberMessage.add(r.multiply(privateKey.secret)).multiply(Math.inv(randNum, curve.N)).mod(curve.N);
        return new Signature(r, s);
    }

    public static Signature sign(String message, PrivateKey privateKey) {
        try {
            return Ecdsa.sign(message, privateKey, MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find SHA-256 message digest in provided java environment");
        }
    }

    public static boolean verify(String message, Signature signature, PublicKey publicKey, MessageDigest hashfunc) {
        Point u2;
        byte[] hashMessage = hashfunc.digest(message.getBytes());
        BigInteger numberMessage = BinaryAscii.numberFromString(hashMessage);
        Curve curve = publicKey.curve;
        BigInteger r = signature.r;
        BigInteger s = signature.s;
        if (r.compareTo(new BigInteger(String.valueOf(1))) < 0) {
            return false;
        }
        if (r.compareTo(curve.N) >= 0) {
            return false;
        }
        if (s.compareTo(new BigInteger(String.valueOf(1))) < 0) {
            return false;
        }
        if (s.compareTo(curve.N) >= 0) {
            return false;
        }
        BigInteger w = Math.inv(s, curve.N);
        Point u1 = Math.multiply(curve.G, numberMessage.multiply(w).mod(curve.N), curve.N, curve.A, curve.P);
        Point v = Math.add(u1, u2 = Math.multiply(publicKey.point, r.multiply(w).mod(curve.N), curve.N, curve.A, curve.P), curve.A, curve.P);
        if (v.isAtInfinity()) {
            return false;
        }
        return v.x.mod(curve.N).equals(r);
    }

    public static boolean verify(String message, Signature signature, PublicKey publicKey) {
        try {
            return Ecdsa.verify(message, signature, publicKey, MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not find SHA-256 message digest in provided java environment");
        }
    }
}

