/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;
import org.bouncycastle.util.Fingerprint;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

class Utils {
    Utils() {
    }

    static boolean isValidPrefix(byte[] byArray, byte[] byArray2) {
        if (byArray2.length < byArray.length) {
            return !Utils.isValidPrefix(byArray, byArray);
        }
        int n = 0;
        for (int i = 0; i != byArray.length; ++i) {
            n |= byArray[i] ^ byArray2[i];
        }
        return n == 0;
    }

    static String keyToString(String string, String string2, AsymmetricKeyParameter asymmetricKeyParameter) {
        StringBuffer stringBuffer = new StringBuffer();
        String string3 = Strings.lineSeparator();
        byte[] byArray = asymmetricKeyParameter instanceof X448PublicKeyParameters ? ((X448PublicKeyParameters)asymmetricKeyParameter).getEncoded() : (asymmetricKeyParameter instanceof Ed448PublicKeyParameters ? ((Ed448PublicKeyParameters)asymmetricKeyParameter).getEncoded() : (asymmetricKeyParameter instanceof X25519PublicKeyParameters ? ((X25519PublicKeyParameters)asymmetricKeyParameter).getEncoded() : ((Ed25519PublicKeyParameters)asymmetricKeyParameter).getEncoded()));
        stringBuffer.append(string2).append(" ").append(string).append(" [").append(Utils.generateKeyFingerprint(byArray)).append("]").append(string3).append("    public data: ").append(Hex.toHexString(byArray)).append(string3);
        return stringBuffer.toString();
    }

    private static String generateKeyFingerprint(byte[] byArray) {
        return new Fingerprint(byArray).toString();
    }
}

