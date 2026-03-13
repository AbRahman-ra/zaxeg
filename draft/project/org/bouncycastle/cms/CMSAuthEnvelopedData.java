/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.AuthEnvelopedData;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.AuthAttributesProvider;
import org.bouncycastle.cms.CMSEnvelopedHelper;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSecureReadable;
import org.bouncycastle.cms.CMSUtils;
import org.bouncycastle.cms.OriginatorInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Encodable;

public class CMSAuthEnvelopedData
implements Encodable {
    RecipientInformationStore recipientInfoStore;
    ContentInfo contentInfo;
    private OriginatorInformation originatorInfo;
    private AlgorithmIdentifier authEncAlg;
    private ASN1Set authAttrs;
    private byte[] mac;
    private ASN1Set unauthAttrs;

    public CMSAuthEnvelopedData(byte[] byArray) throws CMSException {
        this(CMSUtils.readContentInfo(byArray));
    }

    public CMSAuthEnvelopedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSAuthEnvelopedData(ContentInfo contentInfo) throws CMSException {
        this.contentInfo = contentInfo;
        AuthEnvelopedData authEnvelopedData = AuthEnvelopedData.getInstance(contentInfo.getContent());
        if (authEnvelopedData.getOriginatorInfo() != null) {
            this.originatorInfo = new OriginatorInformation(authEnvelopedData.getOriginatorInfo());
        }
        ASN1Set aSN1Set = authEnvelopedData.getRecipientInfos();
        final EncryptedContentInfo encryptedContentInfo = authEnvelopedData.getAuthEncryptedContentInfo();
        this.authEncAlg = encryptedContentInfo.getContentEncryptionAlgorithm();
        this.mac = authEnvelopedData.getMac().getOctets();
        CMSSecureReadable cMSSecureReadable = new CMSSecureReadable(){

            public ASN1ObjectIdentifier getContentType() {
                return encryptedContentInfo.getContentType();
            }

            public InputStream getInputStream() throws IOException, CMSException {
                return new ByteArrayInputStream(Arrays.concatenate(encryptedContentInfo.getEncryptedContent().getOctets(), CMSAuthEnvelopedData.this.mac));
            }
        };
        this.authAttrs = authEnvelopedData.getAuthAttrs();
        this.unauthAttrs = authEnvelopedData.getUnauthAttrs();
        this.recipientInfoStore = this.authAttrs != null ? CMSEnvelopedHelper.buildRecipientInformationStore(aSN1Set, this.authEncAlg, cMSSecureReadable, new AuthAttributesProvider(){

            public ASN1Set getAuthAttributes() {
                return CMSAuthEnvelopedData.this.authAttrs;
            }

            public boolean isAead() {
                return true;
            }
        }) : CMSEnvelopedHelper.buildRecipientInformationStore(aSN1Set, this.authEncAlg, cMSSecureReadable);
    }

    public OriginatorInformation getOriginatorInfo() {
        return this.originatorInfo;
    }

    public RecipientInformationStore getRecipientInfos() {
        return this.recipientInfoStore;
    }

    public AttributeTable getAuthAttrs() {
        if (this.authAttrs == null) {
            return null;
        }
        return new AttributeTable(this.authAttrs);
    }

    public AttributeTable getUnauthAttrs() {
        if (this.unauthAttrs == null) {
            return null;
        }
        return new AttributeTable(this.unauthAttrs);
    }

    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }

    public ContentInfo toASN1Structure() {
        return this.contentInfo;
    }

    public byte[] getEncoded() throws IOException {
        return this.contentInfo.getEncoded();
    }
}

