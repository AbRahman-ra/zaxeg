/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.SSHBuffer;
import org.bouncycastle.crypto.util.SSHBuilder;
import org.bouncycastle.crypto.util.SSHNamedCurves;
import org.bouncycastle.math.ec.ECCurve;

public class OpenSSHPublicKeyUtil {
    private static final String RSA = "ssh-rsa";
    private static final String ECDSA = "ecdsa";
    private static final String ED_25519 = "ssh-ed25519";
    private static final String DSS = "ssh-dss";

    private OpenSSHPublicKeyUtil() {
    }

    public static AsymmetricKeyParameter parsePublicKey(byte[] byArray) {
        SSHBuffer sSHBuffer = new SSHBuffer(byArray);
        return OpenSSHPublicKeyUtil.parsePublicKey(sSHBuffer);
    }

    public static byte[] encodePublicKey(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("cipherParameters was null.");
        }
        if (asymmetricKeyParameter instanceof RSAKeyParameters) {
            if (asymmetricKeyParameter.isPrivate()) {
                throw new IllegalArgumentException("RSAKeyParamaters was for encryption");
            }
            RSAKeyParameters rSAKeyParameters = (RSAKeyParameters)asymmetricKeyParameter;
            SSHBuilder sSHBuilder = new SSHBuilder();
            sSHBuilder.writeString(RSA);
            sSHBuilder.writeBigNum(rSAKeyParameters.getExponent());
            sSHBuilder.writeBigNum(rSAKeyParameters.getModulus());
            return sSHBuilder.getBytes();
        }
        if (asymmetricKeyParameter instanceof ECPublicKeyParameters) {
            SSHBuilder sSHBuilder = new SSHBuilder();
            String string = SSHNamedCurves.getNameForParameters(((ECPublicKeyParameters)asymmetricKeyParameter).getParameters());
            if (string == null) {
                throw new IllegalArgumentException("unable to derive ssh curve name for " + ((ECPublicKeyParameters)asymmetricKeyParameter).getParameters().getCurve().getClass().getName());
            }
            sSHBuilder.writeString("ecdsa-sha2-" + string);
            sSHBuilder.writeString(string);
            sSHBuilder.writeBlock(((ECPublicKeyParameters)asymmetricKeyParameter).getQ().getEncoded(false));
            return sSHBuilder.getBytes();
        }
        if (asymmetricKeyParameter instanceof DSAPublicKeyParameters) {
            DSAPublicKeyParameters dSAPublicKeyParameters = (DSAPublicKeyParameters)asymmetricKeyParameter;
            DSAParameters dSAParameters = dSAPublicKeyParameters.getParameters();
            SSHBuilder sSHBuilder = new SSHBuilder();
            sSHBuilder.writeString(DSS);
            sSHBuilder.writeBigNum(dSAParameters.getP());
            sSHBuilder.writeBigNum(dSAParameters.getQ());
            sSHBuilder.writeBigNum(dSAParameters.getG());
            sSHBuilder.writeBigNum(dSAPublicKeyParameters.getY());
            return sSHBuilder.getBytes();
        }
        if (asymmetricKeyParameter instanceof Ed25519PublicKeyParameters) {
            SSHBuilder sSHBuilder = new SSHBuilder();
            sSHBuilder.writeString(ED_25519);
            sSHBuilder.writeBlock(((Ed25519PublicKeyParameters)asymmetricKeyParameter).getEncoded());
            return sSHBuilder.getBytes();
        }
        throw new IllegalArgumentException("unable to convert " + asymmetricKeyParameter.getClass().getName() + " to private key");
    }

    public static AsymmetricKeyParameter parsePublicKey(SSHBuffer sSHBuffer) {
        AsymmetricKeyParameter asymmetricKeyParameter = null;
        String string = sSHBuffer.readString();
        if (RSA.equals(string)) {
            BigInteger bigInteger = sSHBuffer.readBigNumPositive();
            BigInteger bigInteger2 = sSHBuffer.readBigNumPositive();
            asymmetricKeyParameter = new RSAKeyParameters(false, bigInteger2, bigInteger);
        } else if (DSS.equals(string)) {
            BigInteger bigInteger = sSHBuffer.readBigNumPositive();
            BigInteger bigInteger3 = sSHBuffer.readBigNumPositive();
            BigInteger bigInteger4 = sSHBuffer.readBigNumPositive();
            BigInteger bigInteger5 = sSHBuffer.readBigNumPositive();
            asymmetricKeyParameter = new DSAPublicKeyParameters(bigInteger5, new DSAParameters(bigInteger, bigInteger3, bigInteger4));
        } else if (string.startsWith(ECDSA)) {
            String string2 = sSHBuffer.readString();
            ASN1ObjectIdentifier aSN1ObjectIdentifier = SSHNamedCurves.getByName(string2);
            X9ECParameters x9ECParameters = SSHNamedCurves.getParameters(aSN1ObjectIdentifier);
            if (x9ECParameters == null) {
                throw new IllegalStateException("unable to find curve for " + string + " using curve name " + string2);
            }
            ECCurve eCCurve = x9ECParameters.getCurve();
            byte[] byArray = sSHBuffer.readBlock();
            asymmetricKeyParameter = new ECPublicKeyParameters(eCCurve.decodePoint(byArray), (ECDomainParameters)new ECNamedDomainParameters(aSN1ObjectIdentifier, x9ECParameters));
        } else if (ED_25519.equals(string)) {
            byte[] byArray = sSHBuffer.readBlock();
            if (byArray.length != 32) {
                throw new IllegalStateException("public key value of wrong length");
            }
            asymmetricKeyParameter = new Ed25519PublicKeyParameters(byArray, 0);
        }
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("unable to parse key");
        }
        if (sSHBuffer.hasRemaining()) {
            throw new IllegalArgumentException("decoded key has trailing data");
        }
        return asymmetricKeyParameter;
    }
}

