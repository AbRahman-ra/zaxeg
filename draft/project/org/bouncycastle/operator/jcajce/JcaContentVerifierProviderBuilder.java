/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.operator.jcajce;

import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jcajce.CompositePublicKey;
import org.bouncycastle.jcajce.io.OutputStreamFactory;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.RawContentVerifier;
import org.bouncycastle.operator.RuntimeOperatorException;
import org.bouncycastle.operator.jcajce.OperatorHelper;
import org.bouncycastle.util.io.TeeOutputStream;

public class JcaContentVerifierProviderBuilder {
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());

    public JcaContentVerifierProviderBuilder setProvider(Provider provider) {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));
        return this;
    }

    public JcaContentVerifierProviderBuilder setProvider(String string) {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(string));
        return this;
    }

    public ContentVerifierProvider build(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException, CertificateException {
        return this.build(this.helper.convertCertificate(x509CertificateHolder));
    }

    public ContentVerifierProvider build(final X509Certificate x509Certificate) throws OperatorCreationException {
        JcaX509CertificateHolder jcaX509CertificateHolder;
        try {
            jcaX509CertificateHolder = new JcaX509CertificateHolder(x509Certificate);
        } catch (CertificateEncodingException certificateEncodingException) {
            throw new OperatorCreationException("cannot process certificate: " + certificateEncodingException.getMessage(), certificateEncodingException);
        }
        return new ContentVerifierProvider(){

            public boolean hasAssociatedCertificate() {
                return true;
            }

            public X509CertificateHolder getAssociatedCertificate() {
                return jcaX509CertificateHolder;
            }

            public ContentVerifier get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                Signature signature;
                if (algorithmIdentifier.getAlgorithm().equals(MiscObjectIdentifiers.id_alg_composite)) {
                    return JcaContentVerifierProviderBuilder.this.createCompositeVerifier(algorithmIdentifier, x509Certificate.getPublicKey());
                }
                try {
                    signature = JcaContentVerifierProviderBuilder.this.helper.createSignature(algorithmIdentifier);
                    signature.initVerify(x509Certificate.getPublicKey());
                } catch (GeneralSecurityException generalSecurityException) {
                    throw new OperatorCreationException("exception on setup: " + generalSecurityException, generalSecurityException);
                }
                Signature signature2 = JcaContentVerifierProviderBuilder.this.createRawSig(algorithmIdentifier, x509Certificate.getPublicKey());
                if (signature2 != null) {
                    return new RawSigVerifier(algorithmIdentifier, signature, signature2);
                }
                return new SigVerifier(algorithmIdentifier, signature);
            }
        };
    }

    public ContentVerifierProvider build(final PublicKey publicKey) throws OperatorCreationException {
        return new ContentVerifierProvider(){

            public boolean hasAssociatedCertificate() {
                return false;
            }

            public X509CertificateHolder getAssociatedCertificate() {
                return null;
            }

            public ContentVerifier get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                if (algorithmIdentifier.getAlgorithm().equals(MiscObjectIdentifiers.id_alg_composite)) {
                    return JcaContentVerifierProviderBuilder.this.createCompositeVerifier(algorithmIdentifier, publicKey);
                }
                if (publicKey instanceof CompositePublicKey) {
                    List<PublicKey> list = ((CompositePublicKey)publicKey).getPublicKeys();
                    for (int i = 0; i != list.size(); ++i) {
                        try {
                            Signature signature = JcaContentVerifierProviderBuilder.this.createSignature(algorithmIdentifier, list.get(i));
                            Signature signature2 = JcaContentVerifierProviderBuilder.this.createRawSig(algorithmIdentifier, list.get(i));
                            if (signature2 != null) {
                                return new RawSigVerifier(algorithmIdentifier, signature, signature2);
                            }
                            return new SigVerifier(algorithmIdentifier, signature);
                        } catch (OperatorCreationException operatorCreationException) {
                            continue;
                        }
                    }
                    throw new OperatorCreationException("no matching algorithm found for key");
                }
                Signature signature = JcaContentVerifierProviderBuilder.this.createSignature(algorithmIdentifier, publicKey);
                Signature signature3 = JcaContentVerifierProviderBuilder.this.createRawSig(algorithmIdentifier, publicKey);
                if (signature3 != null) {
                    return new RawSigVerifier(algorithmIdentifier, signature, signature3);
                }
                return new SigVerifier(algorithmIdentifier, signature);
            }
        };
    }

    public ContentVerifierProvider build(SubjectPublicKeyInfo subjectPublicKeyInfo) throws OperatorCreationException {
        return this.build(this.helper.convertPublicKey(subjectPublicKeyInfo));
    }

    private ContentVerifier createCompositeVerifier(AlgorithmIdentifier algorithmIdentifier, PublicKey publicKey) throws OperatorCreationException {
        if (publicKey instanceof CompositePublicKey) {
            List<PublicKey> list = ((CompositePublicKey)publicKey).getPublicKeys();
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(algorithmIdentifier.getParameters());
            Signature[] signatureArray = new Signature[aSN1Sequence.size()];
            for (int i = 0; i != aSN1Sequence.size(); ++i) {
                AlgorithmIdentifier algorithmIdentifier2 = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i));
                signatureArray[i] = list.get(i) != null ? this.createSignature(algorithmIdentifier2, list.get(i)) : null;
            }
            return new CompositeVerifier(signatureArray);
        }
        ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(algorithmIdentifier.getParameters());
        Signature[] signatureArray = new Signature[aSN1Sequence.size()];
        for (int i = 0; i != aSN1Sequence.size(); ++i) {
            AlgorithmIdentifier algorithmIdentifier3 = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i));
            try {
                signatureArray[i] = this.createSignature(algorithmIdentifier3, publicKey);
                continue;
            } catch (Exception exception) {
                signatureArray[i] = null;
            }
        }
        return new CompositeVerifier(signatureArray);
    }

    private Signature createSignature(AlgorithmIdentifier algorithmIdentifier, PublicKey publicKey) throws OperatorCreationException {
        try {
            Signature signature = this.helper.createSignature(algorithmIdentifier);
            signature.initVerify(publicKey);
            return signature;
        } catch (GeneralSecurityException generalSecurityException) {
            throw new OperatorCreationException("exception on setup: " + generalSecurityException, generalSecurityException);
        }
    }

    private Signature createRawSig(AlgorithmIdentifier algorithmIdentifier, PublicKey publicKey) {
        Signature signature;
        try {
            signature = this.helper.createRawSignature(algorithmIdentifier);
            if (signature != null) {
                signature.initVerify(publicKey);
            }
        } catch (Exception exception) {
            signature = null;
        }
        return signature;
    }

    private class CompositeVerifier
    implements ContentVerifier {
        private Signature[] sigs;
        private OutputStream stream;

        public CompositeVerifier(Signature[] signatureArray) throws OperatorCreationException {
            int n;
            this.sigs = signatureArray;
            for (n = 0; n < signatureArray.length && signatureArray[n] == null; ++n) {
            }
            if (n == signatureArray.length) {
                throw new OperatorCreationException("no matching signature found in composite");
            }
            this.stream = OutputStreamFactory.createStream(signatureArray[n]);
            for (int i = n + 1; i != signatureArray.length; ++i) {
                if (signatureArray[i] == null) continue;
                this.stream = new TeeOutputStream(this.stream, OutputStreamFactory.createStream(signatureArray[i]));
            }
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return new AlgorithmIdentifier(MiscObjectIdentifiers.id_alg_composite);
        }

        public OutputStream getOutputStream() {
            return this.stream;
        }

        public boolean verify(byte[] byArray) {
            try {
                ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(byArray);
                boolean bl = false;
                for (int i = 0; i != aSN1Sequence.size(); ++i) {
                    if (this.sigs[i] == null || this.sigs[i].verify(DERBitString.getInstance(aSN1Sequence.getObjectAt(i)).getBytes())) continue;
                    bl = true;
                }
                return !bl;
            } catch (SignatureException signatureException) {
                throw new RuntimeOperatorException("exception obtaining signature: " + signatureException.getMessage(), signatureException);
            }
        }
    }

    private class RawSigVerifier
    extends SigVerifier
    implements RawContentVerifier {
        private Signature rawSignature;

        RawSigVerifier(AlgorithmIdentifier algorithmIdentifier, Signature signature, Signature signature2) {
            super(algorithmIdentifier, signature);
            this.rawSignature = signature2;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public boolean verify(byte[] byArray) {
            try {
                boolean bl = super.verify(byArray);
                return bl;
            } finally {
                try {
                    this.rawSignature.verify(byArray);
                } catch (Exception exception) {}
            }
        }

        public boolean verify(byte[] byArray, byte[] byArray2) {
            try {
                this.rawSignature.update(byArray);
                boolean bl = this.rawSignature.verify(byArray2);
                return bl;
            } catch (SignatureException signatureException) {
                throw new RuntimeOperatorException("exception obtaining raw signature: " + signatureException.getMessage(), signatureException);
            } finally {
                try {
                    this.rawSignature.verify(byArray2);
                } catch (Exception exception) {}
            }
        }
    }

    private class SigVerifier
    implements ContentVerifier {
        private final AlgorithmIdentifier algorithm;
        private final Signature signature;
        protected final OutputStream stream;

        SigVerifier(AlgorithmIdentifier algorithmIdentifier, Signature signature) {
            this.algorithm = algorithmIdentifier;
            this.signature = signature;
            this.stream = OutputStreamFactory.createStream(signature);
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return this.algorithm;
        }

        public OutputStream getOutputStream() {
            if (this.stream == null) {
                throw new IllegalStateException("verifier not initialised");
            }
            return this.stream;
        }

        public boolean verify(byte[] byArray) {
            try {
                return this.signature.verify(byArray);
            } catch (SignatureException signatureException) {
                throw new RuntimeOperatorException("exception obtaining signature: " + signatureException.getMessage(), signatureException);
            }
        }
    }
}

