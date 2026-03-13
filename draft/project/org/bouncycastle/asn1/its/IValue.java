/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.util.BigIntegers;

public class IValue
extends ASN1Object {
    private final BigInteger value;

    private IValue(ASN1Integer aSN1Integer) {
        int n = BigIntegers.intValueExact(aSN1Integer.getValue());
        if (n < 0 || n > 65535) {
            throw new IllegalArgumentException("value out of range");
        }
        this.value = aSN1Integer.getValue();
    }

    public static IValue getInstance(Object object) {
        if (object instanceof IValue) {
            return (IValue)object;
        }
        if (object != null) {
            return new IValue(ASN1Integer.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return new ASN1Integer(this.value);
    }
}

