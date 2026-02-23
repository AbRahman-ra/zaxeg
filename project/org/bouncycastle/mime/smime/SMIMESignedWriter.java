/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeWriter;
import org.bouncycastle.mime.encoding.Base64OutputStream;
import org.bouncycastle.mime.smime.SMimeUtils;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.Strings;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SMIMESignedWriter
extends MimeWriter {
    public static final Map RFC3851_MICALGS;
    public static final Map RFC5751_MICALGS;
    public static final Map STANDARD_MICALGS;
    private final CMSSignedDataStreamGenerator sigGen;
    private final String boundary;
    private final OutputStream mimeOut;
    private final String contentTransferEncoding;

    private SMIMESignedWriter(Builder builder, Map<String, String> map, String string, OutputStream outputStream) {
        super(new Headers(SMIMESignedWriter.mapToLines(map), builder.contentTransferEncoding));
        this.sigGen = builder.sigGen;
        this.contentTransferEncoding = builder.contentTransferEncoding;
        this.boundary = string;
        this.mimeOut = outputStream;
    }

    @Override
    public OutputStream getContentStream() throws IOException {
        this.headers.dumpHeaders(this.mimeOut);
        this.mimeOut.write(Strings.toByteArray("\r\n"));
        if (this.boundary == null) {
            return null;
        }
        this.mimeOut.write(Strings.toByteArray("This is an S/MIME signed message\r\n"));
        this.mimeOut.write(Strings.toByteArray("\r\n--"));
        this.mimeOut.write(Strings.toByteArray(this.boundary));
        this.mimeOut.write(Strings.toByteArray("\r\n"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Base64OutputStream base64OutputStream = new Base64OutputStream(byteArrayOutputStream);
        return new ContentOutputStream(this.sigGen.open(base64OutputStream, false, SMimeUtils.createUnclosable(this.mimeOut)), this.mimeOut, byteArrayOutputStream, base64OutputStream);
    }

    static {
        HashMap<ASN1ObjectIdentifier, String> hashMap = new HashMap<ASN1ObjectIdentifier, String>();
        hashMap.put(CMSAlgorithm.MD5, "md5");
        hashMap.put(CMSAlgorithm.SHA1, "sha-1");
        hashMap.put(CMSAlgorithm.SHA224, "sha-224");
        hashMap.put(CMSAlgorithm.SHA256, "sha-256");
        hashMap.put(CMSAlgorithm.SHA384, "sha-384");
        hashMap.put(CMSAlgorithm.SHA512, "sha-512");
        hashMap.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC5751_MICALGS = Collections.unmodifiableMap(hashMap);
        HashMap<ASN1ObjectIdentifier, String> hashMap2 = new HashMap<ASN1ObjectIdentifier, String>();
        hashMap2.put(CMSAlgorithm.MD5, "md5");
        hashMap2.put(CMSAlgorithm.SHA1, "sha1");
        hashMap2.put(CMSAlgorithm.SHA224, "sha224");
        hashMap2.put(CMSAlgorithm.SHA256, "sha256");
        hashMap2.put(CMSAlgorithm.SHA384, "sha384");
        hashMap2.put(CMSAlgorithm.SHA512, "sha512");
        hashMap2.put(CMSAlgorithm.GOST3411, "gostr3411-94");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_256, "gostr3411-2012-256");
        hashMap2.put(CMSAlgorithm.GOST3411_2012_512, "gostr3411-2012-512");
        RFC3851_MICALGS = Collections.unmodifiableMap(hashMap2);
        STANDARD_MICALGS = RFC5751_MICALGS;
    }

    public static class Builder {
        private static final String[] detHeaders = new String[]{"Content-Type"};
        private static final String[] detValues = new String[]{"multipart/signed; protocol=\"application/pkcs7-signature\""};
        private static final String[] encHeaders = new String[]{"Content-Type", "Content-Disposition", "Content-Transfer-Encoding", "Content-Description"};
        private static final String[] encValues = new String[]{"application/pkcs7-mime; name=\"smime.p7m\"; smime-type=enveloped-data", "attachment; filename=\"smime.p7m\"", "base64", "S/MIME Signed Message"};
        private final CMSSignedDataStreamGenerator sigGen = new CMSSignedDataStreamGenerator();
        private final Map<String, String> extraHeaders = new LinkedHashMap<String, String>();
        private final boolean encapsulated;
        private final Map micAlgs = STANDARD_MICALGS;
        String contentTransferEncoding = "base64";

        public Builder() {
            this(false);
        }

        public Builder(boolean bl) {
            this.encapsulated = bl;
        }

        public Builder withHeader(String string, String string2) {
            this.extraHeaders.put(string, string2);
            return this;
        }

        public Builder addCertificate(X509CertificateHolder x509CertificateHolder) throws CMSException {
            this.sigGen.addCertificate(x509CertificateHolder);
            return this;
        }

        public Builder addCertificates(Store store) throws CMSException {
            this.sigGen.addCertificates(store);
            return this;
        }

        public Builder addSignerInfoGenerator(SignerInfoGenerator signerInfoGenerator) {
            this.sigGen.addSignerInfoGenerator(signerInfoGenerator);
            return this;
        }

        public SMIMESignedWriter build(OutputStream outputStream) {
            String string;
            LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
            if (this.encapsulated) {
                string = null;
                for (int i = 0; i != encHeaders.length; ++i) {
                    linkedHashMap.put(encHeaders[i], encValues[i]);
                }
            } else {
                string = this.generateBoundary();
                StringBuffer stringBuffer = new StringBuffer(detValues[0]);
                this.addHashHeader(stringBuffer, this.sigGen.getDigestAlgorithms());
                this.addBoundary(stringBuffer, string);
                linkedHashMap.put(detHeaders[0], stringBuffer.toString());
                for (int i = 1; i < detHeaders.length; ++i) {
                    linkedHashMap.put(detHeaders[i], detValues[i]);
                }
            }
            for (Map.Entry<String, String> entry : this.extraHeaders.entrySet()) {
                linkedHashMap.put(entry.getKey(), entry.getValue());
            }
            return new SMIMESignedWriter(this, linkedHashMap, string, SMimeUtils.autoBuffer(outputStream));
        }

        private void addHashHeader(StringBuffer stringBuffer, List list) {
            int n = 0;
            Iterator iterator = list.iterator();
            TreeSet<String> treeSet = new TreeSet<String>();
            while (iterator.hasNext()) {
                AlgorithmIdentifier object = (AlgorithmIdentifier)iterator.next();
                String string = (String)this.micAlgs.get(object.getAlgorithm());
                if (string == null) {
                    treeSet.add("unknown");
                    continue;
                }
                treeSet.add(string);
            }
            for (String string : treeSet) {
                if (n == 0) {
                    if (treeSet.size() != 1) {
                        stringBuffer.append("; micalg=\"");
                    } else {
                        stringBuffer.append("; micalg=");
                    }
                } else {
                    stringBuffer.append(',');
                }
                stringBuffer.append(string);
                ++n;
            }
            if (n != 0 && treeSet.size() != 1) {
                stringBuffer.append('\"');
            }
        }

        private void addBoundary(StringBuffer stringBuffer, String string) {
            stringBuffer.append(";\r\n\tboundary=\"");
            stringBuffer.append(string);
            stringBuffer.append("\"");
        }

        private String generateBoundary() {
            SecureRandom secureRandom = new SecureRandom();
            return "==" + new BigInteger(180, secureRandom).setBit(179).toString(16) + "=";
        }
    }

    private class ContentOutputStream
    extends OutputStream {
        private final OutputStream main;
        private final OutputStream backing;
        private final ByteArrayOutputStream sigStream;
        private final OutputStream sigBase;

        ContentOutputStream(OutputStream outputStream, OutputStream outputStream2, ByteArrayOutputStream byteArrayOutputStream, OutputStream outputStream3) {
            this.main = outputStream;
            this.backing = outputStream2;
            this.sigStream = byteArrayOutputStream;
            this.sigBase = outputStream3;
        }

        public void write(byte[] byArray) throws IOException {
            this.main.write(byArray);
        }

        public void write(byte[] byArray, int n, int n2) throws IOException {
            this.main.write(byArray, n, n2);
        }

        public void write(int n) throws IOException {
            this.main.write(n);
        }

        public void close() throws IOException {
            if (SMIMESignedWriter.this.boundary != null) {
                this.main.close();
                this.backing.write(Strings.toByteArray("\r\n--"));
                this.backing.write(Strings.toByteArray(SMIMESignedWriter.this.boundary));
                this.backing.write(Strings.toByteArray("\r\n"));
                this.backing.write(Strings.toByteArray("Content-Type: application/pkcs7-signature; name=\"smime.p7s\"\r\n"));
                this.backing.write(Strings.toByteArray("Content-Transfer-Encoding: base64\r\n"));
                this.backing.write(Strings.toByteArray("Content-Disposition: attachment; filename=\"smime.p7s\"\r\n"));
                this.backing.write(Strings.toByteArray("\r\n"));
                if (this.sigBase != null) {
                    this.sigBase.close();
                }
                this.backing.write(this.sigStream.toByteArray());
                this.backing.write(Strings.toByteArray("\r\n--"));
                this.backing.write(Strings.toByteArray(SMIMESignedWriter.this.boundary));
                this.backing.write(Strings.toByteArray("--\r\n"));
            }
            if (this.backing != null) {
                this.backing.close();
            }
        }
    }
}

