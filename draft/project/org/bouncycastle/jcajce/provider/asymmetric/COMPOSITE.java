/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.CompositePrivateKey;
import org.bouncycastle.jcajce.CompositePublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseKeyFactorySpi;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;

public class COMPOSITE {
    private static final String PREFIX = "org.bouncycastle.jcajce.provider.asymmetric.COMPOSITE";
    private static final Map<String, String> compositeAttributes = new HashMap<String, String>();
    private static AsymmetricKeyInfoConverter baseConverter;

    static {
        compositeAttributes.put("SupportedKeyClasses", "org.bouncycastle.jcajce.CompositePublicKey|org.bouncycastle.jcajce.CompositePrivateKey");
        compositeAttributes.put("SupportedKeyFormats", "PKCS#8|X.509");
    }

    private static class CompositeKeyInfoConverter
    implements AsymmetricKeyInfoConverter {
        private final ConfigurableProvider provider;

        public CompositeKeyInfoConverter(ConfigurableProvider configurableProvider) {
            this.provider = configurableProvider;
        }

        public PrivateKey generatePrivate(PrivateKeyInfo privateKeyInfo) throws IOException {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(privateKeyInfo.getPrivateKey().getOctets());
            PrivateKey[] privateKeyArray = new PrivateKey[aSN1Sequence.size()];
            for (int i = 0; i != aSN1Sequence.size(); ++i) {
                PrivateKeyInfo privateKeyInfo2 = PrivateKeyInfo.getInstance(aSN1Sequence.getObjectAt(i));
                privateKeyArray[i] = this.provider.getKeyInfoConverter(privateKeyInfo2.getPrivateKeyAlgorithm().getAlgorithm()).generatePrivate(privateKeyInfo2);
            }
            return new CompositePrivateKey(privateKeyArray);
        }

        public PublicKey generatePublic(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(subjectPublicKeyInfo.getPublicKeyData().getBytes());
            PublicKey[] publicKeyArray = new PublicKey[aSN1Sequence.size()];
            for (int i = 0; i != aSN1Sequence.size(); ++i) {
                SubjectPublicKeyInfo subjectPublicKeyInfo2 = SubjectPublicKeyInfo.getInstance(aSN1Sequence.getObjectAt(i));
                publicKeyArray[i] = this.provider.getKeyInfoConverter(subjectPublicKeyInfo2.getAlgorithm().getAlgorithm()).generatePublic(subjectPublicKeyInfo2);
            }
            return new CompositePublicKey(publicKeyArray);
        }
    }

    public static class KeyFactory
    extends BaseKeyFactorySpi {
        protected Key engineTranslateKey(Key key) throws InvalidKeyException {
            try {
                if (key instanceof PrivateKey) {
                    return this.generatePrivate(PrivateKeyInfo.getInstance(key.getEncoded()));
                }
                if (key instanceof PublicKey) {
                    return this.generatePublic(SubjectPublicKeyInfo.getInstance(key.getEncoded()));
                }
            } catch (IOException iOException) {
                throw new InvalidKeyException("key could not be parsed: " + iOException.getMessage());
            }
            throw new InvalidKeyException("key not recognized");
        }

        public PrivateKey generatePrivate(PrivateKeyInfo privateKeyInfo) throws IOException {
            return baseConverter.generatePrivate(privateKeyInfo);
        }

        public PublicKey generatePublic(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
            return baseConverter.generatePublic(subjectPublicKeyInfo);
        }
    }

    public static class Mappings
    extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("KeyFactory.COMPOSITE", "org.bouncycastle.jcajce.provider.asymmetric.COMPOSITE$KeyFactory");
            configurableProvider.addAlgorithm("KeyFactory." + MiscObjectIdentifiers.id_alg_composite, "org.bouncycastle.jcajce.provider.asymmetric.COMPOSITE$KeyFactory");
            configurableProvider.addAlgorithm("KeyFactory.OID." + MiscObjectIdentifiers.id_alg_composite, "org.bouncycastle.jcajce.provider.asymmetric.COMPOSITE$KeyFactory");
            baseConverter = new CompositeKeyInfoConverter(configurableProvider);
            configurableProvider.addKeyInfoConverter(MiscObjectIdentifiers.id_alg_composite, baseConverter);
        }
    }
}

