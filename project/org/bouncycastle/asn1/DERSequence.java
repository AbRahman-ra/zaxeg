/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.StreamUtil;

public class DERSequence
extends ASN1Sequence {
    private int bodyLength = -1;

    public static DERSequence convert(ASN1Sequence aSN1Sequence) {
        return (DERSequence)aSN1Sequence.toDERObject();
    }

    public DERSequence() {
    }

    public DERSequence(ASN1Encodable aSN1Encodable) {
        super(aSN1Encodable);
    }

    public DERSequence(ASN1EncodableVector aSN1EncodableVector) {
        super(aSN1EncodableVector);
    }

    public DERSequence(ASN1Encodable[] aSN1EncodableArray) {
        super(aSN1EncodableArray);
    }

    DERSequence(ASN1Encodable[] aSN1EncodableArray, boolean bl) {
        super(aSN1EncodableArray, bl);
    }

    private int getBodyLength() throws IOException {
        if (this.bodyLength < 0) {
            int n = this.elements.length;
            int n2 = 0;
            for (int i = 0; i < n; ++i) {
                ASN1Primitive aSN1Primitive = this.elements[i].toASN1Primitive().toDERObject();
                n2 += aSN1Primitive.encodedLength();
            }
            this.bodyLength = n2;
        }
        return this.bodyLength;
    }

    int encodedLength() throws IOException {
        int n = this.getBodyLength();
        return 1 + StreamUtil.calculateBodyLength(n) + n;
    }

    void encode(ASN1OutputStream aSN1OutputStream, boolean bl) throws IOException {
        if (bl) {
            aSN1OutputStream.write(48);
        }
        DEROutputStream dEROutputStream = aSN1OutputStream.getDERSubStream();
        int n = this.elements.length;
        if (this.bodyLength >= 0 || n > 16) {
            aSN1OutputStream.writeLength(this.getBodyLength());
            for (int i = 0; i < n; ++i) {
                ASN1Primitive aSN1Primitive = this.elements[i].toASN1Primitive().toDERObject();
                aSN1Primitive.encode(dEROutputStream, true);
            }
        } else {
            int n2;
            int n3 = 0;
            ASN1Primitive[] aSN1PrimitiveArray = new ASN1Primitive[n];
            for (n2 = 0; n2 < n; ++n2) {
                ASN1Primitive aSN1Primitive;
                aSN1PrimitiveArray[n2] = aSN1Primitive = this.elements[n2].toASN1Primitive().toDERObject();
                n3 += aSN1Primitive.encodedLength();
            }
            this.bodyLength = n3;
            aSN1OutputStream.writeLength(n3);
            for (n2 = 0; n2 < n; ++n2) {
                aSN1PrimitiveArray[n2].encode(dEROutputStream, true);
            }
        }
    }

    ASN1Primitive toDERObject() {
        return this;
    }

    ASN1Primitive toDLObject() {
        return this;
    }
}

