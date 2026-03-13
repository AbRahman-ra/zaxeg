/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.jcajce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.util.Integers;

public class JcaX509ExtensionUtils
extends X509ExtensionUtils {
    public JcaX509ExtensionUtils() throws NoSuchAlgorithmException {
        super(new SHA1DigestCalculator(MessageDigest.getInstance("SHA1")));
    }

    public JcaX509ExtensionUtils(DigestCalculator digestCalculator) {
        super(digestCalculator);
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(X509Certificate x509Certificate) throws CertificateEncodingException {
        return super.createAuthorityKeyIdentifier(new JcaX509CertificateHolder(x509Certificate));
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(PublicKey publicKey) {
        return super.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(PublicKey publicKey, X500Principal x500Principal, BigInteger bigInteger) {
        return super.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), new GeneralNames(new GeneralName(X500Name.getInstance(x500Principal.getEncoded()))), bigInteger);
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(PublicKey publicKey, GeneralNames generalNames, BigInteger bigInteger) {
        return super.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), generalNames, bigInteger);
    }

    public SubjectKeyIdentifier createSubjectKeyIdentifier(PublicKey publicKey) {
        return super.createSubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public SubjectKeyIdentifier createTruncatedSubjectKeyIdentifier(PublicKey publicKey) {
        return super.createTruncatedSubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public static ASN1Primitive parseExtensionValue(byte[] byArray) throws IOException {
        return ASN1Primitive.fromByteArray(ASN1OctetString.getInstance(byArray).getOctets());
    }

    public static Collection getIssuerAlternativeNames(X509Certificate x509Certificate) throws CertificateParsingException {
        byte[] byArray = x509Certificate.getExtensionValue(Extension.issuerAlternativeName.getId());
        return JcaX509ExtensionUtils.getAlternativeNames(byArray);
    }

    public static Collection getSubjectAlternativeNames(X509Certificate x509Certificate) throws CertificateParsingException {
        byte[] byArray = x509Certificate.getExtensionValue(Extension.subjectAlternativeName.getId());
        return JcaX509ExtensionUtils.getAlternativeNames(byArray);
    }

    private static Collection getAlternativeNames(byte[] byArray) throws CertificateParsingException {
        if (byArray == null) {
            return Collections.EMPTY_LIST;
        }
        try {
            ArrayList arrayList = new ArrayList();
            Enumeration enumeration = DERSequence.getInstance(JcaX509ExtensionUtils.parseExtensionValue(byArray)).getObjects();
            while (enumeration.hasMoreElements()) {
                GeneralName generalName = GeneralName.getInstance(enumeration.nextElement());
                ArrayList<Object> arrayList2 = new ArrayList<Object>();
                arrayList2.add(Integers.valueOf(generalName.getTagNo()));
                switch (generalName.getTagNo()) {
                    case 0: 
                    case 3: 
                    case 5: {
                        arrayList2.add(generalName.getName().toASN1Primitive());
                        break;
                    }
                    case 4: {
                        arrayList2.add(X500Name.getInstance(generalName.getName()).toString());
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
                        arrayList2.add(DEROctetString.getInstance(generalName.getName()).getOctets());
                        break;
                    }
                    default: {
                        throw new IOException("Bad tag number: " + generalName.getTagNo());
                    }
                }
                arrayList.add(arrayList2);
            }
            return Collections.unmodifiableCollection(arrayList);
        } catch (Exception exception) {
            throw new CertificateParsingException(exception.getMessage());
        }
    }

    private static class SHA1DigestCalculator
    implements DigestCalculator {
        private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        private MessageDigest digest;

        public SHA1DigestCalculator(MessageDigest messageDigest) {
            this.digest = messageDigest;
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
        }

        public OutputStream getOutputStream() {
            return this.bOut;
        }

        public byte[] getDigest() {
            byte[] byArray = this.digest.digest(this.bOut.toByteArray());
            this.bOut.reset();
            return byArray;
        }
    }
}

