/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Exception;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObjectParser;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLTaggedObject;

public abstract class ASN1TaggedObject
extends ASN1Primitive
implements ASN1TaggedObjectParser {
    final int tagNo;
    final boolean explicit;
    final ASN1Encodable obj;

    public static ASN1TaggedObject getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        if (bl) {
            return ASN1TaggedObject.getInstance(aSN1TaggedObject.getObject());
        }
        throw new IllegalArgumentException("implicitly tagged tagged object");
    }

    public static ASN1TaggedObject getInstance(Object object) {
        if (object == null || object instanceof ASN1TaggedObject) {
            return (ASN1TaggedObject)object;
        }
        if (object instanceof byte[]) {
            try {
                return ASN1TaggedObject.getInstance(ASN1TaggedObject.fromByteArray((byte[])object));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("failed to construct tagged object from byte[]: " + iOException.getMessage());
            }
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + object.getClass().getName());
    }

    public ASN1TaggedObject(boolean bl, int n, ASN1Encodable aSN1Encodable) {
        if (null == aSN1Encodable) {
            throw new NullPointerException("'obj' cannot be null");
        }
        this.tagNo = n;
        this.explicit = bl || aSN1Encodable instanceof ASN1Choice;
        this.obj = aSN1Encodable;
    }

    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        ASN1Primitive aSN1Primitive2;
        if (!(aSN1Primitive instanceof ASN1TaggedObject)) {
            return false;
        }
        ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject)aSN1Primitive;
        if (this.tagNo != aSN1TaggedObject.tagNo || this.explicit != aSN1TaggedObject.explicit) {
            return false;
        }
        ASN1Primitive aSN1Primitive3 = this.obj.toASN1Primitive();
        return aSN1Primitive3 == (aSN1Primitive2 = aSN1TaggedObject.obj.toASN1Primitive()) || aSN1Primitive3.asn1Equals(aSN1Primitive2);
    }

    public int hashCode() {
        return this.tagNo ^ (this.explicit ? 15 : 240) ^ this.obj.toASN1Primitive().hashCode();
    }

    public int getTagNo() {
        return this.tagNo;
    }

    public boolean isExplicit() {
        return this.explicit;
    }

    public ASN1Primitive getObject() {
        return this.obj.toASN1Primitive();
    }

    public ASN1Encodable getObjectParser(int n, boolean bl) throws IOException {
        switch (n) {
            case 17: {
                return ASN1Set.getInstance(this, bl).parser();
            }
            case 16: {
                return ASN1Sequence.getInstance(this, bl).parser();
            }
            case 4: {
                return ASN1OctetString.getInstance(this, bl).parser();
            }
        }
        if (bl) {
            return this.getObject();
        }
        throw new ASN1Exception("implicit tagging not implemented for tag: " + n);
    }

    public ASN1Primitive getLoadedObject() {
        return this.toASN1Primitive();
    }

    ASN1Primitive toDERObject() {
        return new DERTaggedObject(this.explicit, this.tagNo, this.obj);
    }

    ASN1Primitive toDLObject() {
        return new DLTaggedObject(this.explicit, this.tagNo, this.obj);
    }

    abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

    public String toString() {
        return "[" + this.tagNo + "]" + this.obj;
    }
}

