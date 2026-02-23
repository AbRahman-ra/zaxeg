/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.pkcs;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.pkcs.ContentInfo;

public class AuthenticatedSafe
extends ASN1Object {
    private ContentInfo[] info;
    private boolean isBer = true;

    private AuthenticatedSafe(ASN1Sequence aSN1Sequence) {
        this.info = new ContentInfo[aSN1Sequence.size()];
        for (int i = 0; i != this.info.length; ++i) {
            this.info[i] = ContentInfo.getInstance(aSN1Sequence.getObjectAt(i));
        }
        this.isBer = aSN1Sequence instanceof BERSequence;
    }

    public static AuthenticatedSafe getInstance(Object object) {
        if (object instanceof AuthenticatedSafe) {
            return (AuthenticatedSafe)object;
        }
        if (object != null) {
            return new AuthenticatedSafe(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public AuthenticatedSafe(ContentInfo[] contentInfoArray) {
        this.info = this.copy(contentInfoArray);
    }

    public ContentInfo[] getContentInfo() {
        return this.copy(this.info);
    }

    private ContentInfo[] copy(ContentInfo[] contentInfoArray) {
        ContentInfo[] contentInfoArray2 = new ContentInfo[contentInfoArray.length];
        System.arraycopy(contentInfoArray, 0, contentInfoArray2, 0, contentInfoArray2.length);
        return contentInfoArray2;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.isBer) {
            return new BERSequence(this.info);
        }
        return new DLSequence(this.info);
    }
}

