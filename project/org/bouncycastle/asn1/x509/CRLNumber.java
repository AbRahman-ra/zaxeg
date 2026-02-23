/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x509;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.util.BigIntegers;

public class CRLNumber
extends ASN1Object {
    private BigInteger number;

    public CRLNumber(BigInteger bigInteger) {
        if (BigIntegers.ZERO.compareTo(bigInteger) > 0) {
            throw new IllegalArgumentException("Invalid CRL number : not in (0..MAX)");
        }
        this.number = bigInteger;
    }

    public BigInteger getCRLNumber() {
        return this.number;
    }

    public String toString() {
        return "CRLNumber: " + this.getCRLNumber();
    }

    public ASN1Primitive toASN1Primitive() {
        return new ASN1Integer(this.number);
    }

    public static CRLNumber getInstance(Object object) {
        if (object instanceof CRLNumber) {
            return (CRLNumber)object;
        }
        if (object != null) {
            return new CRLNumber(ASN1Integer.getInstance(object).getValue());
        }
        return null;
    }
}

