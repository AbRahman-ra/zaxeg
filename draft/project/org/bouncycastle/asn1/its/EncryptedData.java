/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.its;

import org.bouncycastle.asn1.ASN1Sequence;

public class EncryptedData {
    private EncryptedData(ASN1Sequence aSN1Sequence) {
    }

    public static EncryptedData getInstance(Object object) {
        if (object instanceof EncryptedData) {
            return (EncryptedData)object;
        }
        if (object != null) {
            return new EncryptedData(ASN1Sequence.getInstance(object));
        }
        return null;
    }
}

