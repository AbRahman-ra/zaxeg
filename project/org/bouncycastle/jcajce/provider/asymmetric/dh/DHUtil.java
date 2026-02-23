/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.dh;

import java.math.BigInteger;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Fingerprint;
import org.bouncycastle.util.Strings;

class DHUtil {
    DHUtil() {
    }

    static String privateKeyToString(String string, BigInteger bigInteger, DHParameters dHParameters) {
        StringBuffer stringBuffer = new StringBuffer();
        String string2 = Strings.lineSeparator();
        BigInteger bigInteger2 = dHParameters.getG().modPow(bigInteger, dHParameters.getP());
        stringBuffer.append(string);
        stringBuffer.append(" Private Key [").append(DHUtil.generateKeyFingerprint(bigInteger2, dHParameters)).append("]").append(string2);
        stringBuffer.append("              Y: ").append(bigInteger2.toString(16)).append(string2);
        return stringBuffer.toString();
    }

    static String publicKeyToString(String string, BigInteger bigInteger, DHParameters dHParameters) {
        StringBuffer stringBuffer = new StringBuffer();
        String string2 = Strings.lineSeparator();
        stringBuffer.append(string);
        stringBuffer.append(" Public Key [").append(DHUtil.generateKeyFingerprint(bigInteger, dHParameters)).append("]").append(string2);
        stringBuffer.append("             Y: ").append(bigInteger.toString(16)).append(string2);
        return stringBuffer.toString();
    }

    private static String generateKeyFingerprint(BigInteger bigInteger, DHParameters dHParameters) {
        return new Fingerprint(Arrays.concatenate(bigInteger.toByteArray(), dHParameters.getP().toByteArray(), dHParameters.getG().toByteArray())).toString();
    }
}

