/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.gost;

import java.math.BigInteger;
import org.bouncycastle.crypto.params.GOST3410Parameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Fingerprint;
import org.bouncycastle.util.Strings;

class GOSTUtil {
    GOSTUtil() {
    }

    static String privateKeyToString(String string, BigInteger bigInteger, GOST3410Parameters gOST3410Parameters) {
        StringBuffer stringBuffer = new StringBuffer();
        String string2 = Strings.lineSeparator();
        BigInteger bigInteger2 = gOST3410Parameters.getA().modPow(bigInteger, gOST3410Parameters.getP());
        stringBuffer.append(string);
        stringBuffer.append(" Private Key [").append(GOSTUtil.generateKeyFingerprint(bigInteger2, gOST3410Parameters)).append("]").append(string2);
        stringBuffer.append("                  Y: ").append(bigInteger2.toString(16)).append(string2);
        return stringBuffer.toString();
    }

    static String publicKeyToString(String string, BigInteger bigInteger, GOST3410Parameters gOST3410Parameters) {
        StringBuffer stringBuffer = new StringBuffer();
        String string2 = Strings.lineSeparator();
        stringBuffer.append(string);
        stringBuffer.append(" Public Key [").append(GOSTUtil.generateKeyFingerprint(bigInteger, gOST3410Parameters)).append("]").append(string2);
        stringBuffer.append("                 Y: ").append(bigInteger.toString(16)).append(string2);
        return stringBuffer.toString();
    }

    private static String generateKeyFingerprint(BigInteger bigInteger, GOST3410Parameters gOST3410Parameters) {
        return new Fingerprint(Arrays.concatenate(bigInteger.toByteArray(), gOST3410Parameters.getP().toByteArray(), gOST3410Parameters.getA().toByteArray())).toString();
    }
}

