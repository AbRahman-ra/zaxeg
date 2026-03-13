/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.starkbank.ellipticcurve;

import com.starkbank.ellipticcurve.Curve;
import com.starkbank.ellipticcurve.Math;
import com.starkbank.ellipticcurve.Point;
import com.starkbank.ellipticcurve.utils.BinaryAscii;
import com.starkbank.ellipticcurve.utils.ByteString;
import com.starkbank.ellipticcurve.utils.Der;
import java.util.Arrays;

public class PublicKey {
    public Point point;
    public Curve curve;

    public PublicKey(Point point, Curve curve) {
        this.point = point;
        this.curve = curve;
    }

    public ByteString toByteString() {
        return this.toByteString(false);
    }

    public ByteString toByteString(boolean encoded) {
        ByteString xStr = BinaryAscii.stringFromNumber(this.point.x, this.curve.length());
        ByteString yStr = BinaryAscii.stringFromNumber(this.point.y, this.curve.length());
        xStr.insert(yStr.getBytes());
        if (encoded) {
            xStr.insert(0, new byte[]{0, 4});
        }
        return xStr;
    }

    public ByteString toDer() {
        long[] oidEcPublicKey = new long[]{1L, 2L, 840L, 10045L, 2L, 1L};
        ByteString encodeEcAndOid = Der.encodeSequence(Der.encodeOid(oidEcPublicKey), Der.encodeOid(this.curve.oid));
        return Der.encodeSequence(encodeEcAndOid, Der.encodeBitString(this.toByteString(true)));
    }

    public String toPem() {
        return Der.toPem(this.toDer(), "PUBLIC KEY");
    }

    public static PublicKey fromPem(String string) {
        return PublicKey.fromDer(Der.fromPem(string));
    }

    public static PublicKey fromDer(ByteString string) {
        ByteString[] str = Der.removeSequence(string);
        ByteString s1 = str[0];
        ByteString empty = str[1];
        if (!empty.isEmpty()) {
            throw new RuntimeException(String.format("trailing junk after DER pubkey: %s", BinaryAscii.hexFromBinary(empty)));
        }
        str = Der.removeSequence(s1);
        ByteString s2 = str[0];
        ByteString pointStrBitstring = str[1];
        Object[] o = Der.removeObject(s2);
        ByteString rest = (ByteString)o[1];
        o = Der.removeObject(rest);
        long[] oidCurve = (long[])o[0];
        empty = (ByteString)o[1];
        if (!empty.isEmpty()) {
            throw new RuntimeException(String.format("trailing junk after DER pubkey objects: %s", BinaryAscii.hexFromBinary(empty)));
        }
        Curve curve = (Curve)Curve.curvesByOid.get(Arrays.hashCode(oidCurve));
        if (curve == null) {
            throw new RuntimeException(String.format("Unknown curve with oid %s. I only know about these: %s", Arrays.toString(oidCurve), Arrays.toString(Curve.supportedCurves.toArray())));
        }
        str = Der.removeBitString(pointStrBitstring);
        ByteString pointStr = str[0];
        empty = str[1];
        if (!empty.isEmpty()) {
            throw new RuntimeException(String.format("trailing junk after pubkey pointstring: %s", BinaryAscii.hexFromBinary(empty)));
        }
        return PublicKey.fromString(pointStr.substring(2), curve);
    }

    public static PublicKey fromString(ByteString string, Curve curve, boolean validatePoint) {
        int baselen = curve.length();
        ByteString xs = string.substring(0, baselen);
        ByteString ys = string.substring(baselen);
        Point p = new Point(BinaryAscii.numberFromString(xs.getBytes()), BinaryAscii.numberFromString(ys.getBytes()));
        PublicKey publicKey = new PublicKey(p, curve);
        if (!validatePoint) {
            return publicKey;
        }
        if (p.isAtInfinity()) {
            throw new RuntimeException("Public Key point is at infinity");
        }
        if (!curve.contains(p)) {
            throw new RuntimeException(String.format("Point (%s,%s) is not valid for curve %s", p.x, p.y, curve.name));
        }
        if (!Math.multiply(p, curve.N, curve.N, curve.A, curve.P).isAtInfinity()) {
            throw new RuntimeException(String.format("Point (%s,%s) * %s.N is not at infinity", p.x, p.y, curve.name));
        }
        return publicKey;
    }

    public static PublicKey fromString(ByteString string, Curve curve) {
        return PublicKey.fromString(string, curve, true);
    }

    public static PublicKey fromString(ByteString string, boolean validatePoint) {
        return PublicKey.fromString(string, Curve.secp256k1, validatePoint);
    }

    public static PublicKey fromString(ByteString string) {
        return PublicKey.fromString(string, true);
    }
}

