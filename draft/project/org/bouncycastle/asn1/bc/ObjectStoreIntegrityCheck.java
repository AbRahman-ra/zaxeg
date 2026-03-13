/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.bc;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.bc.PbkdMacIntegrityCheck;
import org.bouncycastle.asn1.bc.SignatureCheck;

public class ObjectStoreIntegrityCheck
extends ASN1Object
implements ASN1Choice {
    public static final int PBKD_MAC_CHECK = 0;
    public static final int SIG_CHECK = 1;
    private final int type;
    private final ASN1Object integrityCheck;

    public ObjectStoreIntegrityCheck(PbkdMacIntegrityCheck pbkdMacIntegrityCheck) {
        this((ASN1Encodable)pbkdMacIntegrityCheck);
    }

    public ObjectStoreIntegrityCheck(SignatureCheck signatureCheck) {
        this(new DERTaggedObject(0, signatureCheck));
    }

    private ObjectStoreIntegrityCheck(ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable instanceof ASN1Sequence || aSN1Encodable instanceof PbkdMacIntegrityCheck) {
            this.type = 0;
            this.integrityCheck = PbkdMacIntegrityCheck.getInstance(aSN1Encodable);
        } else if (aSN1Encodable instanceof ASN1TaggedObject) {
            this.type = 1;
            this.integrityCheck = SignatureCheck.getInstance(((ASN1TaggedObject)aSN1Encodable).getObject());
        } else {
            throw new IllegalArgumentException("Unknown check object in integrity check.");
        }
    }

    public static ObjectStoreIntegrityCheck getInstance(Object object) {
        if (object instanceof ObjectStoreIntegrityCheck) {
            return (ObjectStoreIntegrityCheck)object;
        }
        if (object instanceof byte[]) {
            try {
                return new ObjectStoreIntegrityCheck(ASN1Primitive.fromByteArray((byte[])object));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("Unable to parse integrity check details.");
            }
        }
        if (object != null) {
            return new ObjectStoreIntegrityCheck((ASN1Encodable)object);
        }
        return null;
    }

    public int getType() {
        return this.type;
    }

    public ASN1Object getIntegrityCheck() {
        return this.integrityCheck;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.integrityCheck instanceof SignatureCheck) {
            return new DERTaggedObject(0, this.integrityCheck);
        }
        return this.integrityCheck.toASN1Primitive();
    }
}

