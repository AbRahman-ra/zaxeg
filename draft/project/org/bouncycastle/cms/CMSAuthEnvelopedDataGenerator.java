/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.AuthEnvelopedData;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSAuthEnvelopedData;
import org.bouncycastle.cms.CMSAuthEnvelopedGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OutputAEADEncryptor;

public class CMSAuthEnvelopedDataGenerator
extends CMSAuthEnvelopedGenerator {
    private CMSAuthEnvelopedData doGenerate(CMSTypedData cMSTypedData, OutputAEADEncryptor outputAEADEncryptor) throws CMSException {
        Object object;
        Object object22;
        Object object3;
        Object object4;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DERSet dERSet = null;
        try {
            object4 = outputAEADEncryptor.getOutputStream(byteArrayOutputStream);
            cMSTypedData.write((OutputStream)object4);
            if (this.authAttrsGenerator != null) {
                object3 = this.authAttrsGenerator.getAttributes(new HashMap());
                dERSet = new DERSet(((AttributeTable)object3).toASN1EncodableVector());
                outputAEADEncryptor.getAADStream().write(dERSet.getEncoded("DER"));
            }
            ((OutputStream)object4).close();
        } catch (IOException iOException) {
            throw new CMSException("unable to process authenticated content: " + iOException.getMessage(), iOException);
        }
        object4 = byteArrayOutputStream.toByteArray();
        object3 = outputAEADEncryptor.getMAC();
        AlgorithmIdentifier algorithmIdentifier = outputAEADEncryptor.getAlgorithmIdentifier();
        BEROctetString bEROctetString = new BEROctetString((byte[])object4);
        GenericKey genericKey = outputAEADEncryptor.getKey();
        for (Object object22 : this.recipientInfoGenerators) {
            aSN1EncodableVector.add(object22.generate(genericKey));
        }
        EncryptedContentInfo encryptedContentInfo = new EncryptedContentInfo(cMSTypedData.getContentType(), algorithmIdentifier, bEROctetString);
        object22 = null;
        if (this.unauthAttrsGenerator != null) {
            object = this.unauthAttrsGenerator.getAttributes(new HashMap());
            object22 = new DLSet(((AttributeTable)object).toASN1EncodableVector());
        }
        object = new ContentInfo(CMSObjectIdentifiers.authEnvelopedData, new AuthEnvelopedData(this.originatorInfo, new DERSet(aSN1EncodableVector), encryptedContentInfo, dERSet, new DEROctetString((byte[])object3), (ASN1Set)object22));
        return new CMSAuthEnvelopedData((ContentInfo)object);
    }

    public CMSAuthEnvelopedData generate(CMSTypedData cMSTypedData, OutputAEADEncryptor outputAEADEncryptor) throws CMSException {
        return this.doGenerate(cMSTypedData, outputAEADEncryptor);
    }
}

