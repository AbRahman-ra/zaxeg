/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SSHBuffer;
import org.bouncycastle.crypto.util.SSHBuilder;
import org.bouncycastle.crypto.util.SSHNamedCurves;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Strings;

public class OpenSSHPrivateKeyUtil {
    static final byte[] AUTH_MAGIC = Strings.toByteArray("openssh-key-v1\u0000");

    private OpenSSHPrivateKeyUtil() {
    }

    public static byte[] encodePrivateKey(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("param is null");
        }
        if (asymmetricKeyParameter instanceof RSAPrivateCrtKeyParameters) {
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricKeyParameter);
            return privateKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        }
        if (asymmetricKeyParameter instanceof ECPrivateKeyParameters) {
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricKeyParameter);
            return privateKeyInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        }
        if (asymmetricKeyParameter instanceof DSAPrivateKeyParameters) {
            DSAPrivateKeyParameters dSAPrivateKeyParameters = (DSAPrivateKeyParameters)asymmetricKeyParameter;
            DSAParameters dSAParameters = dSAPrivateKeyParameters.getParameters();
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            aSN1EncodableVector.add(new ASN1Integer(0L));
            aSN1EncodableVector.add(new ASN1Integer(dSAParameters.getP()));
            aSN1EncodableVector.add(new ASN1Integer(dSAParameters.getQ()));
            aSN1EncodableVector.add(new ASN1Integer(dSAParameters.getG()));
            BigInteger bigInteger = dSAParameters.getG().modPow(dSAPrivateKeyParameters.getX(), dSAParameters.getP());
            aSN1EncodableVector.add(new ASN1Integer(bigInteger));
            aSN1EncodableVector.add(new ASN1Integer(dSAPrivateKeyParameters.getX()));
            try {
                return new DERSequence(aSN1EncodableVector).getEncoded();
            } catch (Exception exception) {
                throw new IllegalStateException("unable to encode DSAPrivateKeyParameters " + exception.getMessage());
            }
        }
        if (asymmetricKeyParameter instanceof Ed25519PrivateKeyParameters) {
            Ed25519PublicKeyParameters ed25519PublicKeyParameters = ((Ed25519PrivateKeyParameters)asymmetricKeyParameter).generatePublicKey();
            SSHBuilder sSHBuilder = new SSHBuilder();
            sSHBuilder.writeBytes(AUTH_MAGIC);
            sSHBuilder.writeString("none");
            sSHBuilder.writeString("none");
            sSHBuilder.writeString("");
            sSHBuilder.u32(1);
            Object object = OpenSSHPublicKeyUtil.encodePublicKey(ed25519PublicKeyParameters);
            sSHBuilder.writeBlock((byte[])object);
            object = new SSHBuilder();
            int n = CryptoServicesRegistrar.getSecureRandom().nextInt();
            ((SSHBuilder)object).u32(n);
            ((SSHBuilder)object).u32(n);
            ((SSHBuilder)object).writeString("ssh-ed25519");
            byte[] byArray = ed25519PublicKeyParameters.getEncoded();
            ((SSHBuilder)object).writeBlock(byArray);
            ((SSHBuilder)object).writeBlock(Arrays.concatenate(((Ed25519PrivateKeyParameters)asymmetricKeyParameter).getEncoded(), byArray));
            ((SSHBuilder)object).writeString("");
            sSHBuilder.writeBlock(((SSHBuilder)object).getPaddedBytes());
            return sSHBuilder.getBytes();
        }
        throw new IllegalArgumentException("unable to convert " + asymmetricKeyParameter.getClass().getName() + " to openssh private key");
    }

    public static AsymmetricKeyParameter parsePrivateKeyBlob(byte[] byArray) {
        AsymmetricKeyParameter asymmetricKeyParameter = null;
        if (byArray[0] == 48) {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(byArray);
            if (aSN1Sequence.size() == 6) {
                if (OpenSSHPrivateKeyUtil.allIntegers(aSN1Sequence) && ((ASN1Integer)aSN1Sequence.getObjectAt(0)).getPositiveValue().equals(BigIntegers.ZERO)) {
                    asymmetricKeyParameter = new DSAPrivateKeyParameters(((ASN1Integer)aSN1Sequence.getObjectAt(5)).getPositiveValue(), new DSAParameters(((ASN1Integer)aSN1Sequence.getObjectAt(1)).getPositiveValue(), ((ASN1Integer)aSN1Sequence.getObjectAt(2)).getPositiveValue(), ((ASN1Integer)aSN1Sequence.getObjectAt(3)).getPositiveValue()));
                }
            } else if (aSN1Sequence.size() == 9) {
                if (OpenSSHPrivateKeyUtil.allIntegers(aSN1Sequence) && ((ASN1Integer)aSN1Sequence.getObjectAt(0)).getPositiveValue().equals(BigIntegers.ZERO)) {
                    RSAPrivateKey rSAPrivateKey = RSAPrivateKey.getInstance(aSN1Sequence);
                    asymmetricKeyParameter = new RSAPrivateCrtKeyParameters(rSAPrivateKey.getModulus(), rSAPrivateKey.getPublicExponent(), rSAPrivateKey.getPrivateExponent(), rSAPrivateKey.getPrime1(), rSAPrivateKey.getPrime2(), rSAPrivateKey.getExponent1(), rSAPrivateKey.getExponent2(), rSAPrivateKey.getCoefficient());
                }
            } else if (aSN1Sequence.size() == 4 && aSN1Sequence.getObjectAt(3) instanceof ASN1TaggedObject && aSN1Sequence.getObjectAt(2) instanceof ASN1TaggedObject) {
                ECPrivateKey eCPrivateKey = ECPrivateKey.getInstance(aSN1Sequence);
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)eCPrivateKey.getParameters();
                X9ECParameters x9ECParameters = ECNamedCurveTable.getByOID(aSN1ObjectIdentifier);
                asymmetricKeyParameter = new ECPrivateKeyParameters(eCPrivateKey.getKey(), (ECDomainParameters)new ECNamedDomainParameters(aSN1ObjectIdentifier, x9ECParameters));
            }
        } else {
            int n;
            SSHBuffer sSHBuffer = new SSHBuffer(AUTH_MAGIC, byArray);
            String string = sSHBuffer.readString();
            if (!"none".equals(string)) {
                throw new IllegalStateException("encrypted keys not supported");
            }
            sSHBuffer.skipBlock();
            sSHBuffer.skipBlock();
            int n2 = sSHBuffer.readU32();
            if (n2 != 1) {
                throw new IllegalStateException("multiple keys not supported");
            }
            OpenSSHPublicKeyUtil.parsePublicKey(sSHBuffer.readBlock());
            byte[] byArray2 = sSHBuffer.readPaddedBlock();
            if (sSHBuffer.hasRemaining()) {
                throw new IllegalArgumentException("decoded key has trailing data");
            }
            SSHBuffer sSHBuffer2 = new SSHBuffer(byArray2);
            int n3 = sSHBuffer2.readU32();
            if (n3 != (n = sSHBuffer2.readU32())) {
                throw new IllegalStateException("private key check values are not the same");
            }
            String string2 = sSHBuffer2.readString();
            if ("ssh-ed25519".equals(string2)) {
                sSHBuffer2.readBlock();
                byte[] byArray3 = sSHBuffer2.readBlock();
                if (byArray3.length != 64) {
                    throw new IllegalStateException("private key value of wrong length");
                }
                asymmetricKeyParameter = new Ed25519PrivateKeyParameters(byArray3, 0);
            } else if (string2.startsWith("ecdsa")) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = SSHNamedCurves.getByName(Strings.fromByteArray(sSHBuffer2.readBlock()));
                if (aSN1ObjectIdentifier == null) {
                    throw new IllegalStateException("OID not found for: " + string2);
                }
                X9ECParameters x9ECParameters = NISTNamedCurves.getByOID(aSN1ObjectIdentifier);
                if (x9ECParameters == null) {
                    throw new IllegalStateException("Curve not found for: " + aSN1ObjectIdentifier);
                }
                sSHBuffer2.readBlock();
                byte[] byArray4 = sSHBuffer2.readBlock();
                asymmetricKeyParameter = new ECPrivateKeyParameters(new BigInteger(1, byArray4), (ECDomainParameters)new ECNamedDomainParameters(aSN1ObjectIdentifier, x9ECParameters));
            }
            sSHBuffer2.skipBlock();
            if (sSHBuffer2.hasRemaining()) {
                throw new IllegalArgumentException("private key block has trailing data");
            }
        }
        if (asymmetricKeyParameter == null) {
            throw new IllegalArgumentException("unable to parse key");
        }
        return asymmetricKeyParameter;
    }

    private static boolean allIntegers(ASN1Sequence aSN1Sequence) {
        for (int i = 0; i < aSN1Sequence.size(); ++i) {
            if (aSN1Sequence.getObjectAt(i) instanceof ASN1Integer) continue;
            return false;
        }
        return true;
    }
}

