/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

public class SubjectPermissions
extends ASN1Object
implements ASN1Choice {
    public static SubjectPermissions getInstance(Object object) {
        if (object instanceof SubjectPermissions) {
            return (SubjectPermissions)object;
        }
        if (object != null) {
            return null;
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return null;
    }
}

