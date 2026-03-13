/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.StreamUtil;

public class DERTaggedObject
extends ASN1TaggedObject {
    public DERTaggedObject(boolean bl, int n, ASN1Encodable aSN1Encodable) {
        super(bl, n, aSN1Encodable);
    }

    public DERTaggedObject(int n, ASN1Encodable aSN1Encodable) {
        super(true, n, aSN1Encodable);
    }

    boolean isConstructed() {
        return this.explicit || this.obj.toASN1Primitive().toDERObject().isConstructed();
    }

    int encodedLength() throws IOException {
        ASN1Primitive aSN1Primitive = this.obj.toASN1Primitive().toDERObject();
        int n = aSN1Primitive.encodedLength();
        if (this.explicit) {
            return StreamUtil.calculateTagLength(this.tagNo) + StreamUtil.calculateBodyLength(n) + n;
        }
        return StreamUtil.calculateTagLength(this.tagNo) + --n;
    }

    void encode(ASN1OutputStream aSN1OutputStream, boolean bl) throws IOException {
        ASN1Primitive aSN1Primitive = this.obj.toASN1Primitive().toDERObject();
        int n = 128;
        if (this.explicit || aSN1Primitive.isConstructed()) {
            n |= 0x20;
        }
        aSN1OutputStream.writeTag(bl, n, this.tagNo);
        if (this.explicit) {
            aSN1OutputStream.writeLength(aSN1Primitive.encodedLength());
        }
        aSN1Primitive.encode(aSN1OutputStream.getDERSubStream(), this.explicit);
    }

    ASN1Primitive toDERObject() {
        return this;
    }

    ASN1Primitive toDLObject() {
        return this;
    }
}

