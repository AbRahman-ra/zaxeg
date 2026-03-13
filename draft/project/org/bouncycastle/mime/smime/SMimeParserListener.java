/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime.smime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.OriginatorInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.mime.ConstantMimeContext;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeContext;
import org.bouncycastle.mime.MimeIOException;
import org.bouncycastle.mime.MimeParserContext;
import org.bouncycastle.mime.MimeParserListener;
import org.bouncycastle.mime.smime.SMimeMultipartContext;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.io.Streams;

public abstract class SMimeParserListener
implements MimeParserListener {
    private DigestCalculator[] digestCalculators;
    private SMimeMultipartContext parent;

    public MimeContext createContext(MimeParserContext mimeParserContext, Headers headers) {
        if (headers.isMultipart()) {
            this.parent = new SMimeMultipartContext(mimeParserContext, headers);
            this.digestCalculators = this.parent.getDigestCalculators();
            return this.parent;
        }
        return new ConstantMimeContext();
    }

    public void object(MimeParserContext mimeParserContext, Headers headers, InputStream inputStream) throws IOException {
        try {
            if (headers.getContentType().equals("application/pkcs7-signature") || headers.getContentType().equals("application/x-pkcs7-signature")) {
                HashMap<ASN1ObjectIdentifier, byte[]> hashMap = new HashMap<ASN1ObjectIdentifier, byte[]>();
                for (int i = 0; i != this.digestCalculators.length; ++i) {
                    this.digestCalculators[i].getOutputStream().close();
                    hashMap.put(this.digestCalculators[i].getAlgorithmIdentifier().getAlgorithm(), this.digestCalculators[i].getDigest());
                }
                byte[] byArray = Streams.readAll(inputStream);
                CMSSignedData cMSSignedData = new CMSSignedData(hashMap, byArray);
                this.signedData(mimeParserContext, headers, cMSSignedData.getCertificates(), cMSSignedData.getCRLs(), cMSSignedData.getAttributeCertificates(), cMSSignedData.getSignerInfos());
            } else if (headers.getContentType().equals("application/pkcs7-mime") || headers.getContentType().equals("application/x-pkcs7-mime")) {
                CMSEnvelopedDataParser cMSEnvelopedDataParser = new CMSEnvelopedDataParser(inputStream);
                this.envelopedData(mimeParserContext, headers, cMSEnvelopedDataParser.getOriginatorInfo(), cMSEnvelopedDataParser.getRecipientInfos());
                cMSEnvelopedDataParser.close();
            } else {
                this.content(mimeParserContext, headers, inputStream);
            }
        } catch (CMSException cMSException) {
            throw new MimeIOException("CMS failure: " + cMSException.getMessage(), cMSException);
        }
    }

    public void content(MimeParserContext mimeParserContext, Headers headers, InputStream inputStream) throws IOException {
        throw new IllegalStateException("content handling not implemented");
    }

    public void signedData(MimeParserContext mimeParserContext, Headers headers, Store store, Store store2, Store store3, SignerInformationStore signerInformationStore) throws IOException, CMSException {
        throw new IllegalStateException("signedData handling not implemented");
    }

    public void envelopedData(MimeParserContext mimeParserContext, Headers headers, OriginatorInformation originatorInformation, RecipientInformationStore recipientInformationStore) throws IOException, CMSException {
        throw new IllegalStateException("envelopedData handling not implemented");
    }
}

