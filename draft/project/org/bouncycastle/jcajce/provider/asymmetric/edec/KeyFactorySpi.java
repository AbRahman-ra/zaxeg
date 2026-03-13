/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;
import org.bouncycastle.crypto.util.OpenSSHPrivateKeyUtil;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCXDHPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCXDHPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseKeyFactorySpi;
import org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import org.bouncycastle.jcajce.spec.OpenSSHPrivateKeySpec;
import org.bouncycastle.jcajce.spec.OpenSSHPublicKeySpec;
import org.bouncycastle.jcajce.spec.RawEncodedKeySpec;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class KeyFactorySpi
extends BaseKeyFactorySpi
implements AsymmetricKeyInfoConverter {
    static final byte[] x448Prefix = Hex.decode("3042300506032b656f033900");
    static final byte[] x25519Prefix = Hex.decode("302a300506032b656e032100");
    static final byte[] Ed448Prefix = Hex.decode("3043300506032b6571033a00");
    static final byte[] Ed25519Prefix = Hex.decode("302a300506032b6570032100");
    private static final byte x448_type = 111;
    private static final byte x25519_type = 110;
    private static final byte Ed448_type = 113;
    private static final byte Ed25519_type = 112;
    String algorithm;
    private final boolean isXdh;
    private final int specificBase;

    public KeyFactorySpi(String string, boolean bl, int n) {
        this.algorithm = string;
        this.isXdh = bl;
        this.specificBase = n;
    }

    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        throw new InvalidKeyException("key type unknown");
    }

    protected KeySpec engineGetKeySpec(Key key, Class clazz) throws InvalidKeySpecException {
        if (clazz.isAssignableFrom(OpenSSHPrivateKeySpec.class) && key instanceof BCEdDSAPrivateKey) {
            try {
                ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(key.getEncoded());
                ASN1OctetString aSN1OctetString = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(2));
                byte[] byArray = ASN1OctetString.getInstance(ASN1Primitive.fromByteArray(aSN1OctetString.getOctets())).getOctets();
                return new OpenSSHPrivateKeySpec(OpenSSHPrivateKeyUtil.encodePrivateKey(new Ed25519PrivateKeyParameters(byArray)));
            } catch (IOException iOException) {
                throw new InvalidKeySpecException(iOException.getMessage(), iOException.getCause());
            }
        }
        if (clazz.isAssignableFrom(OpenSSHPublicKeySpec.class) && key instanceof BCEdDSAPublicKey) {
            try {
                byte[] byArray = key.getEncoded();
                if (!Arrays.areEqual(Ed25519Prefix, 0, Ed25519Prefix.length, byArray, 0, byArray.length - 32)) {
                    throw new InvalidKeySpecException("Invalid Ed25519 public key encoding");
                }
                Ed25519PublicKeyParameters ed25519PublicKeyParameters = new Ed25519PublicKeyParameters(byArray, Ed25519Prefix.length);
                return new OpenSSHPublicKeySpec(OpenSSHPublicKeyUtil.encodePublicKey(ed25519PublicKeyParameters));
            } catch (IOException iOException) {
                throw new InvalidKeySpecException(iOException.getMessage(), iOException.getCause());
            }
        }
        if (clazz.isAssignableFrom(RawEncodedKeySpec.class)) {
            if (key instanceof XDHPublicKey) {
                return new RawEncodedKeySpec(((XDHPublicKey)key).getUEncoding());
            }
            if (key instanceof EdDSAPublicKey) {
                return new RawEncodedKeySpec(((EdDSAPublicKey)key).getPointEncoding());
            }
        }
        return super.engineGetKeySpec(key, clazz);
    }

    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof OpenSSHPrivateKeySpec) {
            AsymmetricKeyParameter asymmetricKeyParameter = OpenSSHPrivateKeyUtil.parsePrivateKeyBlob(((OpenSSHPrivateKeySpec)keySpec).getEncoded());
            if (asymmetricKeyParameter instanceof Ed25519PrivateKeyParameters) {
                return new BCEdDSAPrivateKey((Ed25519PrivateKeyParameters)asymmetricKeyParameter);
            }
            throw new IllegalStateException("openssh private key not Ed25519 private key");
        }
        return super.engineGeneratePrivate(keySpec);
    }

    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof X509EncodedKeySpec) {
            byte[] byArray = ((X509EncodedKeySpec)keySpec).getEncoded();
            if (this.specificBase == 0 || this.specificBase == byArray[8]) {
                if (byArray[9] == 5 && byArray[10] == 0) {
                    SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(byArray);
                    subjectPublicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()), subjectPublicKeyInfo.getPublicKeyData().getBytes());
                    try {
                        byArray = subjectPublicKeyInfo.getEncoded("DER");
                    } catch (IOException iOException) {
                        throw new InvalidKeySpecException("attempt to reconstruct key failed: " + iOException.getMessage());
                    }
                }
                switch (byArray[8]) {
                    case 111: {
                        return new BCXDHPublicKey(x448Prefix, byArray);
                    }
                    case 110: {
                        return new BCXDHPublicKey(x25519Prefix, byArray);
                    }
                    case 113: {
                        return new BCEdDSAPublicKey(Ed448Prefix, byArray);
                    }
                    case 112: {
                        return new BCEdDSAPublicKey(Ed25519Prefix, byArray);
                    }
                }
                return super.engineGeneratePublic(keySpec);
            }
        } else {
            if (keySpec instanceof RawEncodedKeySpec) {
                byte[] byArray = ((RawEncodedKeySpec)keySpec).getEncoded();
                switch (this.specificBase) {
                    case 111: {
                        return new BCXDHPublicKey(new X448PublicKeyParameters(byArray));
                    }
                    case 110: {
                        return new BCXDHPublicKey(new X25519PublicKeyParameters(byArray));
                    }
                    case 113: {
                        return new BCEdDSAPublicKey(new Ed448PublicKeyParameters(byArray));
                    }
                    case 112: {
                        return new BCEdDSAPublicKey(new Ed25519PublicKeyParameters(byArray));
                    }
                }
                throw new InvalidKeySpecException("factory not a specific type, cannot recognise raw encoding");
            }
            if (keySpec instanceof OpenSSHPublicKeySpec) {
                AsymmetricKeyParameter asymmetricKeyParameter = OpenSSHPublicKeyUtil.parsePublicKey(((OpenSSHPublicKeySpec)keySpec).getEncoded());
                if (asymmetricKeyParameter instanceof Ed25519PublicKeyParameters) {
                    return new BCEdDSAPublicKey(new byte[0], ((Ed25519PublicKeyParameters)asymmetricKeyParameter).getEncoded());
                }
                throw new IllegalStateException("openssh public key not Ed25519 public key");
            }
        }
        return super.engineGeneratePublic(keySpec);
    }

    public PrivateKey generatePrivate(PrivateKeyInfo privateKeyInfo) throws IOException {
        ASN1ObjectIdentifier aSN1ObjectIdentifier = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm();
        if (this.isXdh) {
            if ((this.specificBase == 0 || this.specificBase == 111) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_X448)) {
                return new BCXDHPrivateKey(privateKeyInfo);
            }
            if ((this.specificBase == 0 || this.specificBase == 110) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_X25519)) {
                return new BCXDHPrivateKey(privateKeyInfo);
            }
        } else if (aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed448) || aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed25519)) {
            if ((this.specificBase == 0 || this.specificBase == 113) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed448)) {
                return new BCEdDSAPrivateKey(privateKeyInfo);
            }
            if ((this.specificBase == 0 || this.specificBase == 112) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed25519)) {
                return new BCEdDSAPrivateKey(privateKeyInfo);
            }
        }
        throw new IOException("algorithm identifier " + aSN1ObjectIdentifier + " in key not recognized");
    }

    public PublicKey generatePublic(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        ASN1ObjectIdentifier aSN1ObjectIdentifier = subjectPublicKeyInfo.getAlgorithm().getAlgorithm();
        if (this.isXdh) {
            if ((this.specificBase == 0 || this.specificBase == 111) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_X448)) {
                return new BCXDHPublicKey(subjectPublicKeyInfo);
            }
            if ((this.specificBase == 0 || this.specificBase == 110) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_X25519)) {
                return new BCXDHPublicKey(subjectPublicKeyInfo);
            }
        } else if (aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed448) || aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed25519)) {
            if ((this.specificBase == 0 || this.specificBase == 113) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed448)) {
                return new BCEdDSAPublicKey(subjectPublicKeyInfo);
            }
            if ((this.specificBase == 0 || this.specificBase == 112) && aSN1ObjectIdentifier.equals(EdECObjectIdentifiers.id_Ed25519)) {
                return new BCEdDSAPublicKey(subjectPublicKeyInfo);
            }
        }
        throw new IOException("algorithm identifier " + aSN1ObjectIdentifier + " in key not recognized");
    }

    public static class Ed25519
    extends KeyFactorySpi {
        public Ed25519() {
            super("Ed25519", false, 112);
        }
    }

    public static class Ed448
    extends KeyFactorySpi {
        public Ed448() {
            super("Ed448", false, 113);
        }
    }

    public static class EdDSA
    extends KeyFactorySpi {
        public EdDSA() {
            super("EdDSA", false, 0);
        }
    }

    public static class X25519
    extends KeyFactorySpi {
        public X25519() {
            super("X25519", true, 110);
        }
    }

    public static class X448
    extends KeyFactorySpi {
        public X448() {
            super("X448", true, 111);
        }
    }

    public static class XDH
    extends KeyFactorySpi {
        public XDH() {
            super("XDH", true, 0);
        }
    }
}

