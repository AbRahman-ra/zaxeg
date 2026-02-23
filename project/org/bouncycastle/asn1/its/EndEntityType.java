/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBitString;

public class EndEntityType
extends ASN1Object {
    public static final int app = 128;
    public static final int enrol = 64;
    private final ASN1BitString type;

    public EndEntityType(int n) {
        if (n != 128 && n != 64) {
            throw new IllegalArgumentException("value out of range");
        }
        this.type = new DERBitString(n);
    }

    private EndEntityType(DERBitString dERBitString) {
        this.type = dERBitString;
    }

    public static EndEntityType getInstance(Object object) {
        if (object instanceof EndEntityType) {
            return (EndEntityType)object;
        }
        if (object != null) {
            return new EndEntityType(DERBitString.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.type;
    }
}

