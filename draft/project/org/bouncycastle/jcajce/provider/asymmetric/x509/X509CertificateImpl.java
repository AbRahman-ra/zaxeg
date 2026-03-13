/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.misc.NetscapeRevocationURL;
import org.bouncycastle.asn1.misc.VerisignCzagExtension;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.jcajce.CompositePublicKey;
import org.bouncycastle.jcajce.interfaces.BCX509Certificate;
import org.bouncycastle.jcajce.io.OutputStreamFactory;
import org.bouncycastle.jcajce.provider.asymmetric.x509.SignatureCreator;
import org.bouncycastle.jcajce.provider.asymmetric.x509.X509SignatureUtil;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Strings;

abstract class X509CertificateImpl
extends X509Certificate
implements BCX509Certificate {
    protected JcaJceHelper bcHelper;
    protected Certificate c;
    protected BasicConstraints basicConstraints;
    protected boolean[] keyUsage;
    protected String sigAlgName;
    protected byte[] sigAlgParams;

    X509CertificateImpl(JcaJceHelper jcaJceHelper, Certificate certificate, BasicConstraints basicConstraints, boolean[] blArray, String string, byte[] byArray) {
        this.bcHelper = jcaJceHelper;
        this.c = certificate;
        this.basicConstraints = basicConstraints;
        this.keyUsage = blArray;
        this.sigAlgName = string;
        this.sigAlgParams = byArray;
    }

    public X500Name getIssuerX500Name() {
        return this.c.getIssuer();
    }

    public TBSCertificate getTBSCertificateNative() {
        return this.c.getTBSCertificate();
    }

    public X500Name getSubjectX500Name() {
        return this.c.getSubject();
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        this.checkValidity(new Date());
    }

    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        if (date.getTime() > this.getNotAfter().getTime()) {
            throw new CertificateExpiredException("certificate expired on " + this.c.getEndDate().getTime());
        }
        if (date.getTime() < this.getNotBefore().getTime()) {
            throw new CertificateNotYetValidException("certificate not valid till " + this.c.getStartDate().getTime());
        }
    }

    public int getVersion() {
        return this.c.getVersionNumber();
    }

    public BigInteger getSerialNumber() {
        return this.c.getSerialNumber().getValue();
    }

    public Principal getIssuerDN() {
        return new X509Principal(this.c.getIssuer());
    }

    public X500Principal getIssuerX500Principal() {
        try {
            byte[] byArray = this.c.getIssuer().getEncoded("DER");
            return new X500Principal(byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }

    public Principal getSubjectDN() {
        return new X509Principal(this.c.getSubject());
    }

    public X500Principal getSubjectX500Principal() {
        try {
            byte[] byArray = this.c.getSubject().getEncoded("DER");
            return new X500Principal(byArray);
        } catch (IOException iOException) {
            throw new IllegalStateException("can't encode subject DN");
        }
    }

    public Date getNotBefore() {
        return this.c.getStartDate().getDate();
    }

    public Date getNotAfter() {
        return this.c.getEndDate().getDate();
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        try {
            return this.c.getTBSCertificate().getEncoded("DER");
        } catch (IOException iOException) {
            throw new CertificateEncodingException(iOException.toString());
        }
    }

    public byte[] getSignature() {
        return this.c.getSignature().getOctets();
    }

    public String getSigAlgName() {
        return this.sigAlgName;
    }

    public String getSigAlgOID() {
        return this.c.getSignatureAlgorithm().getAlgorithm().getId();
    }

    public byte[] getSigAlgParams() {
        return Arrays.clone(this.sigAlgParams);
    }

    public boolean[] getIssuerUniqueID() {
        DERBitString dERBitString = this.c.getTBSCertificate().getIssuerUniqueId();
        if (dERBitString != null) {
            byte[] byArray = dERBitString.getBytes();
            boolean[] blArray = new boolean[byArray.length * 8 - dERBitString.getPadBits()];
            for (int i = 0; i != blArray.length; ++i) {
                blArray[i] = (byArray[i / 8] & 128 >>> i % 8) != 0;
            }
            return blArray;
        }
        return null;
    }

    public boolean[] getSubjectUniqueID() {
        DERBitString dERBitString = this.c.getTBSCertificate().getSubjectUniqueId();
        if (dERBitString != null) {
            byte[] byArray = dERBitString.getBytes();
            boolean[] blArray = new boolean[byArray.length * 8 - dERBitString.getPadBits()];
            for (int i = 0; i != blArray.length; ++i) {
                blArray[i] = (byArray[i / 8] & 128 >>> i % 8) != 0;
            }
            return blArray;
        }
        return null;
    }

    public boolean[] getKeyUsage() {
        return Arrays.clone(this.keyUsage);
    }

    public List getExtendedKeyUsage() throws CertificateParsingException {
        byte[] byArray = X509CertificateImpl.getExtensionOctets(this.c, "2.5.29.37");
        if (null == byArray) {
            return null;
        }
        try {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(byArray));
            ArrayList<String> arrayList = new ArrayList<String>();
            for (int i = 0; i != aSN1Sequence.size(); ++i) {
                arrayList.add(((ASN1ObjectIdentifier)aSN1Sequence.getObjectAt(i)).getId());
            }
            return Collections.unmodifiableList(arrayList);
        } catch (Exception exception) {
            throw new CertificateParsingException("error processing extended key usage extension");
        }
    }

    public int getBasicConstraints() {
        if (this.basicConstraints != null) {
            if (this.basicConstraints.isCA()) {
                if (this.basicConstraints.getPathLenConstraint() == null) {
                    return Integer.MAX_VALUE;
                }
                return this.basicConstraints.getPathLenConstraint().intValue();
            }
            return -1;
        }
        return -1;
    }

    public Collection getSubjectAlternativeNames() throws CertificateParsingException {
        return X509CertificateImpl.getAlternativeNames(this.c, Extension.subjectAlternativeName.getId());
    }

    public Collection getIssuerAlternativeNames() throws CertificateParsingException {
        return X509CertificateImpl.getAlternativeNames(this.c, Extension.issuerAlternativeName.getId());
    }

    public Set getCriticalExtensionOIDs() {
        if (this.getVersion() == 3) {
            HashSet<String> hashSet = new HashSet<String>();
            Extensions extensions = this.c.getTBSCertificate().getExtensions();
            if (extensions != null) {
                Enumeration enumeration = extensions.oids();
                while (enumeration.hasMoreElements()) {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)enumeration.nextElement();
                    Extension extension = extensions.getExtension(aSN1ObjectIdentifier);
                    if (!extension.isCritical()) continue;
                    hashSet.add(aSN1ObjectIdentifier.getId());
                }
                return hashSet;
            }
        }
        return null;
    }

    public byte[] getExtensionValue(String string) {
        ASN1OctetString aSN1OctetString = X509CertificateImpl.getExtensionValue(this.c, string);
        if (null != aSN1OctetString) {
            try {
                return aSN1OctetString.getEncoded();
            } catch (Exception exception) {
                throw new IllegalStateException("error parsing " + exception.toString());
            }
        }
        return null;
    }

    public Set getNonCriticalExtensionOIDs() {
        if (this.getVersion() == 3) {
            HashSet<String> hashSet = new HashSet<String>();
            Extensions extensions = this.c.getTBSCertificate().getExtensions();
            if (extensions != null) {
                Enumeration enumeration = extensions.oids();
                while (enumeration.hasMoreElements()) {
                    ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)enumeration.nextElement();
                    Extension extension = extensions.getExtension(aSN1ObjectIdentifier);
                    if (extension.isCritical()) continue;
                    hashSet.add(aSN1ObjectIdentifier.getId());
                }
                return hashSet;
            }
        }
        return null;
    }

    public boolean hasUnsupportedCriticalExtension() {
        Extensions extensions;
        if (this.getVersion() == 3 && (extensions = this.c.getTBSCertificate().getExtensions()) != null) {
            Enumeration enumeration = extensions.oids();
            while (enumeration.hasMoreElements()) {
                Extension extension;
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)enumeration.nextElement();
                if (aSN1ObjectIdentifier.equals(Extension.keyUsage) || aSN1ObjectIdentifier.equals(Extension.certificatePolicies) || aSN1ObjectIdentifier.equals(Extension.policyMappings) || aSN1ObjectIdentifier.equals(Extension.inhibitAnyPolicy) || aSN1ObjectIdentifier.equals(Extension.cRLDistributionPoints) || aSN1ObjectIdentifier.equals(Extension.issuingDistributionPoint) || aSN1ObjectIdentifier.equals(Extension.deltaCRLIndicator) || aSN1ObjectIdentifier.equals(Extension.policyConstraints) || aSN1ObjectIdentifier.equals(Extension.basicConstraints) || aSN1ObjectIdentifier.equals(Extension.subjectAlternativeName) || aSN1ObjectIdentifier.equals(Extension.nameConstraints) || !(extension = extensions.getExtension(aSN1ObjectIdentifier)).isCritical()) continue;
                return true;
            }
        }
        return false;
    }

    public PublicKey getPublicKey() {
        try {
            return BouncyCastleProvider.getPublicKey(this.c.getSubjectPublicKeyInfo());
        } catch (IOException iOException) {
            return null;
        }
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        String string = Strings.lineSeparator();
        stringBuffer.append("  [0]         Version: ").append(this.getVersion()).append(string);
        stringBuffer.append("         SerialNumber: ").append(this.getSerialNumber()).append(string);
        stringBuffer.append("             IssuerDN: ").append(this.getIssuerDN()).append(string);
        stringBuffer.append("           Start Date: ").append(this.getNotBefore()).append(string);
        stringBuffer.append("           Final Date: ").append(this.getNotAfter()).append(string);
        stringBuffer.append("            SubjectDN: ").append(this.getSubjectDN()).append(string);
        stringBuffer.append("           Public Key: ").append(this.getPublicKey()).append(string);
        stringBuffer.append("  Signature Algorithm: ").append(this.getSigAlgName()).append(string);
        X509SignatureUtil.prettyPrintSignature(this.getSignature(), stringBuffer, string);
        Extensions extensions = this.c.getTBSCertificate().getExtensions();
        if (extensions != null) {
            Enumeration enumeration = extensions.oids();
            if (enumeration.hasMoreElements()) {
                stringBuffer.append("       Extensions: \n");
            }
            while (enumeration.hasMoreElements()) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier)enumeration.nextElement();
                Extension extension = extensions.getExtension(aSN1ObjectIdentifier);
                if (extension.getExtnValue() != null) {
                    byte[] byArray = extension.getExtnValue().getOctets();
                    ASN1InputStream aSN1InputStream = new ASN1InputStream(byArray);
                    stringBuffer.append("                       critical(").append(extension.isCritical()).append(") ");
                    try {
                        if (aSN1ObjectIdentifier.equals(Extension.basicConstraints)) {
                            stringBuffer.append(BasicConstraints.getInstance(aSN1InputStream.readObject())).append(string);
                            continue;
                        }
                        if (aSN1ObjectIdentifier.equals(Extension.keyUsage)) {
                            stringBuffer.append(KeyUsage.getInstance(aSN1InputStream.readObject())).append(string);
                            continue;
                        }
                        if (aSN1ObjectIdentifier.equals(MiscObjectIdentifiers.netscapeCertType)) {
                            stringBuffer.append(new NetscapeCertType(DERBitString.getInstance(aSN1InputStream.readObject()))).append(string);
                            continue;
                        }
                        if (aSN1ObjectIdentifier.equals(MiscObjectIdentifiers.netscapeRevocationURL)) {
                            stringBuffer.append(new NetscapeRevocationURL(DERIA5String.getInstance(aSN1InputStream.readObject()))).append(string);
                            continue;
                        }
                        if (aSN1ObjectIdentifier.equals(MiscObjectIdentifiers.verisignCzagExtension)) {
                            stringBuffer.append(new VerisignCzagExtension(DERIA5String.getInstance(aSN1InputStream.readObject()))).append(string);
                            continue;
                        }
                        stringBuffer.append(aSN1ObjectIdentifier.getId());
                        stringBuffer.append(" value = ").append(ASN1Dump.dumpAsString(aSN1InputStream.readObject())).append(string);
                    } catch (Exception exception) {
                        stringBuffer.append(aSN1ObjectIdentifier.getId());
                        stringBuffer.append(" value = ").append("*****").append(string);
                    }
                    continue;
                }
                stringBuffer.append(string);
            }
        }
        return stringBuffer.toString();
    }

    public final void verify(PublicKey publicKey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.doVerify(publicKey, new SignatureCreator(){

            public Signature createSignature(String string) throws NoSuchAlgorithmException {
                try {
                    return X509CertificateImpl.this.bcHelper.createSignature(string);
                } catch (Exception exception) {
                    return Signature.getInstance(string);
                }
            }
        });
    }

    public final void verify(PublicKey publicKey, final String string) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        this.doVerify(publicKey, new SignatureCreator(){

            public Signature createSignature(String string2) throws NoSuchAlgorithmException, NoSuchProviderException {
                if (string != null) {
                    return Signature.getInstance(string2, string);
                }
                return Signature.getInstance(string2);
            }
        });
    }

    public final void verify(PublicKey publicKey, final Provider provider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            this.doVerify(publicKey, new SignatureCreator(){

                public Signature createSignature(String string) throws NoSuchAlgorithmException {
                    if (provider != null) {
                        return Signature.getInstance(string, provider);
                    }
                    return Signature.getInstance(string);
                }
            });
        } catch (NoSuchProviderException noSuchProviderException) {
            throw new NoSuchAlgorithmException("provider issue: " + noSuchProviderException.getMessage());
        }
    }

    private void doVerify(PublicKey publicKey, SignatureCreator signatureCreator) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        if (publicKey instanceof CompositePublicKey && X509SignatureUtil.isCompositeAlgorithm(this.c.getSignatureAlgorithm())) {
            List<PublicKey> list = ((CompositePublicKey)publicKey).getPublicKeys();
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(this.c.getSignatureAlgorithm().getParameters());
            ASN1Sequence aSN1Sequence2 = ASN1Sequence.getInstance(DERBitString.getInstance(this.c.getSignature()).getBytes());
            boolean bl = false;
            for (int i = 0; i != list.size(); ++i) {
                if (list.get(i) == null) continue;
                AlgorithmIdentifier algorithmIdentifier = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i));
                String string = X509SignatureUtil.getSignatureName(algorithmIdentifier);
                Signature signature = signatureCreator.createSignature(string);
                SignatureException signatureException = null;
                try {
                    this.checkSignature(list.get(i), signature, algorithmIdentifier.getParameters(), DERBitString.getInstance(aSN1Sequence2.getObjectAt(i)).getBytes());
                    bl = true;
                } catch (SignatureException signatureException2) {
                    signatureException = signatureException2;
                }
                if (signatureException == null) continue;
                throw signatureException;
            }
            if (!bl) {
                throw new InvalidKeyException("no matching key found");
            }
        } else if (X509SignatureUtil.isCompositeAlgorithm(this.c.getSignatureAlgorithm())) {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(this.c.getSignatureAlgorithm().getParameters());
            ASN1Sequence aSN1Sequence3 = ASN1Sequence.getInstance(DERBitString.getInstance(this.c.getSignature()).getBytes());
            boolean bl = false;
            for (int i = 0; i != aSN1Sequence3.size(); ++i) {
                AlgorithmIdentifier algorithmIdentifier = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i));
                String string = X509SignatureUtil.getSignatureName(algorithmIdentifier);
                SignatureException signatureException = null;
                try {
                    Signature signature = signatureCreator.createSignature(string);
                    this.checkSignature(publicKey, signature, algorithmIdentifier.getParameters(), DERBitString.getInstance(aSN1Sequence3.getObjectAt(i)).getBytes());
                    bl = true;
                } catch (InvalidKeyException invalidKeyException) {
                } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                } catch (SignatureException signatureException3) {
                    signatureException = signatureException3;
                }
                if (signatureException == null) continue;
                throw signatureException;
            }
            if (!bl) {
                throw new InvalidKeyException("no matching key found");
            }
        } else {
            String string = X509SignatureUtil.getSignatureName(this.c.getSignatureAlgorithm());
            Signature signature = signatureCreator.createSignature(string);
            if (publicKey instanceof CompositePublicKey) {
                List<PublicKey> list = ((CompositePublicKey)publicKey).getPublicKeys();
                for (int i = 0; i != list.size(); ++i) {
                    try {
                        this.checkSignature(list.get(i), signature, this.c.getSignatureAlgorithm().getParameters(), this.getSignature());
                        return;
                    } catch (InvalidKeyException invalidKeyException) {
                        continue;
                    }
                }
                throw new InvalidKeyException("no matching signature found");
            }
            this.checkSignature(publicKey, signature, this.c.getSignatureAlgorithm().getParameters(), this.getSignature());
        }
    }

    private void checkSignature(PublicKey publicKey, Signature signature, ASN1Encodable aSN1Encodable, byte[] byArray) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (!this.isAlgIdEqual(this.c.getSignatureAlgorithm(), this.c.getTBSCertificate().getSignature())) {
            throw new CertificateException("signature algorithm in TBS cert not same as outer cert");
        }
        X509SignatureUtil.setSignatureParameters(signature, aSN1Encodable);
        signature.initVerify(publicKey);
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(OutputStreamFactory.createStream(signature), 512);
            this.c.getTBSCertificate().encodeTo(bufferedOutputStream, "DER");
            ((OutputStream)bufferedOutputStream).close();
        } catch (IOException iOException) {
            throw new CertificateEncodingException(iOException.toString());
        }
        if (!signature.verify(byArray)) {
            throw new SignatureException("certificate does not verify with supplied key");
        }
    }

    private boolean isAlgIdEqual(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) {
        if (!algorithmIdentifier.getAlgorithm().equals(algorithmIdentifier2.getAlgorithm())) {
            return false;
        }
        if (Properties.isOverrideSet("org.bouncycastle.x509.allow_absent_equiv_NULL")) {
            if (algorithmIdentifier.getParameters() == null) {
                return algorithmIdentifier2.getParameters() == null || algorithmIdentifier2.getParameters().equals(DERNull.INSTANCE);
            }
            if (algorithmIdentifier2.getParameters() == null) {
                return algorithmIdentifier.getParameters() == null || algorithmIdentifier.getParameters().equals(DERNull.INSTANCE);
            }
        }
        if (algorithmIdentifier.getParameters() != null) {
            return algorithmIdentifier.getParameters().equals(algorithmIdentifier2.getParameters());
        }
        if (algorithmIdentifier2.getParameters() != null) {
            return algorithmIdentifier2.getParameters().equals(algorithmIdentifier.getParameters());
        }
        return true;
    }

    private static Collection getAlternativeNames(Certificate certificate, String string) throws CertificateParsingException {
        byte[] byArray = X509CertificateImpl.getExtensionOctets(certificate, string);
        if (byArray == null) {
            return null;
        }
        try {
            ArrayList arrayList = new ArrayList();
            Enumeration enumeration = ASN1Sequence.getInstance(byArray).getObjects();
            block11: while (enumeration.hasMoreElements()) {
                GeneralName generalName = GeneralName.getInstance(enumeration.nextElement());
                ArrayList<Object> arrayList2 = new ArrayList<Object>();
                arrayList2.add(Integers.valueOf(generalName.getTagNo()));
                switch (generalName.getTagNo()) {
                    case 0: 
                    case 3: 
                    case 5: {
                        arrayList2.add(generalName.getEncoded());
                        break;
                    }
                    case 4: {
                        arrayList2.add(X500Name.getInstance(RFC4519Style.INSTANCE, generalName.getName()).toString());
                        break;
                    }
                    case 1: 
                    case 2: 
                    case 6: {
                        arrayList2.add(((ASN1String)((Object)generalName.getName())).getString());
                        break;
                    }
                    case 8: {
                        arrayList2.add(ASN1ObjectIdentifier.getInstance(generalName.getName()).getId());
                        break;
                    }
                    case 7: {
                        String string2;
                        byte[] byArray2 = DEROctetString.getInstance(generalName.getName()).getOctets();
                        try {
                            string2 = InetAddress.getByAddress(byArray2).getHostAddress();
                        } catch (UnknownHostException unknownHostException) {
                            continue block11;
                        }
                        arrayList2.add(string2);
                        break;
                    }
                    default: {
                        throw new IOException("Bad tag number: " + generalName.getTagNo());
                    }
                }
                arrayList.add(Collections.unmodifiableList(arrayList2));
            }
            if (arrayList.size() == 0) {
                return null;
            }
            return Collections.unmodifiableCollection(arrayList);
        } catch (Exception exception) {
            throw new CertificateParsingException(exception.getMessage());
        }
    }

    protected static byte[] getExtensionOctets(Certificate certificate, String string) {
        ASN1OctetString aSN1OctetString = X509CertificateImpl.getExtensionValue(certificate, string);
        if (null != aSN1OctetString) {
            return aSN1OctetString.getOctets();
        }
        return null;
    }

    protected static ASN1OctetString getExtensionValue(Certificate certificate, String string) {
        Extension extension;
        Extensions extensions = certificate.getTBSCertificate().getExtensions();
        if (null != extensions && null != (extension = extensions.getExtension(new ASN1ObjectIdentifier(string)))) {
            return extension.getExtnValue();
        }
        return null;
    }
}

