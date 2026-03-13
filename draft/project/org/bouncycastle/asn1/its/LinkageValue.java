/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.its.Utils;
import org.bouncycastle.util.Arrays;

public class LinkageValue
extends ASN1Object {
    private final byte[] value;

    private LinkageValue(ASN1OctetString aSN1OctetString) {
        this.value = Arrays.clone(Utils.octetStringFixed(aSN1OctetString.getOctets(), 9));
    }

    public static LinkageValue getInstance(Object object) {
        if (object instanceof LinkageValue) {
            return (LinkageValue)object;
        }
        if (object != null) {
            return new LinkageValue(ASN1OctetString.getInstance(object));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DEROctetString(Arrays.clone(this.value));
    }
}

