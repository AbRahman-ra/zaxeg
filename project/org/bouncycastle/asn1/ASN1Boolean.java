/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;

public class ASN1Boolean
extends ASN1Primitive {
    private static final byte FALSE_VALUE = 0;
    private static final byte TRUE_VALUE = -1;
    public static final ASN1Boolean FALSE = new ASN1Boolean(0);
    public static final ASN1Boolean TRUE = new ASN1Boolean(-1);
    private final byte value;

    public static ASN1Boolean getInstance(Object object) {
        if (object == null || object instanceof ASN1Boolean) {
            return (ASN1Boolean)object;
        }
        if (object instanceof byte[]) {
            byte[] byArray = (byte[])object;
            try {
                return (ASN1Boolean)ASN1Boolean.fromByteArray(byArray);
            } catch (IOException iOException) {
                throw new IllegalArgumentException("failed to construct boolean from byte[]: " + iOException.getMessage());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + object.getClass().getName());
    }

    public static ASN1Boolean getInstance(boolean bl) {
        return bl ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(int n) {
        return n != 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        ASN1Primitive aSN1Primitive = aSN1TaggedObject.getObject();
        if (bl || aSN1Primitive instanceof ASN1Boolean) {
            return ASN1Boolean.getInstance(aSN1Primitive);
        }
        return ASN1Boolean.fromOctetString(ASN1OctetString.getInstance(aSN1Primitive).getOctets());
    }

    private ASN1Boolean(byte by) {
        this.value = by;
    }

    public boolean isTrue() {
        return this.value != 0;
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return 3;
    }

    void encode(ASN1OutputStream aSN1OutputStream, boolean bl) throws IOException {
        aSN1OutputStream.writeEncoded(bl, 1, this.value);
    }

    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1Boolean)) {
            return false;
        }
        ASN1Boolean aSN1Boolean = (ASN1Boolean)aSN1Primitive;
        return this.isTrue() == aSN1Boolean.isTrue();
    }

    public int hashCode() {
        return this.isTrue() ? 1 : 0;
    }

    ASN1Primitive toDERObject() {
        return this.isTrue() ? TRUE : FALSE;
    }

    public String toString() {
        return this.isTrue() ? "TRUE" : "FALSE";
    }

    static ASN1Boolean fromOctetString(byte[] byArray) {
        if (byArray.length != 1) {
            throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
        }
        byte by = byArray[0];
        switch (by) {
            case 0: {
                return FALSE;
            }
            case -1: {
                return TRUE;
            }
        }
        return new ASN1Boolean(by);
    }
}

