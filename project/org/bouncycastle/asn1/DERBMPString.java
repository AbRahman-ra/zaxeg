/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.StreamUtil;
import org.bouncycastle.util.Arrays;

public class DERBMPString
extends ASN1Primitive
implements ASN1String {
    private final char[] string;

    public static DERBMPString getInstance(Object object) {
        if (object == null || object instanceof DERBMPString) {
            return (DERBMPString)object;
        }
        if (object instanceof byte[]) {
            try {
                return (DERBMPString)DERBMPString.fromByteArray((byte[])object);
            } catch (Exception exception) {
                throw new IllegalArgumentException("encoding error in getInstance: " + exception.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + object.getClass().getName());
    }

    public static DERBMPString getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        ASN1Primitive aSN1Primitive = aSN1TaggedObject.getObject();
        if (bl || aSN1Primitive instanceof DERBMPString) {
            return DERBMPString.getInstance(aSN1Primitive);
        }
        return new DERBMPString(ASN1OctetString.getInstance(aSN1Primitive).getOctets());
    }

    DERBMPString(byte[] byArray) {
        if (byArray == null) {
            throw new NullPointerException("'string' cannot be null");
        }
        int n = byArray.length;
        if (0 != (n & 1)) {
            throw new IllegalArgumentException("malformed BMPString encoding encountered");
        }
        int n2 = n / 2;
        char[] cArray = new char[n2];
        for (int i = 0; i != n2; ++i) {
            cArray[i] = (char)(byArray[2 * i] << 8 | byArray[2 * i + 1] & 0xFF);
        }
        this.string = cArray;
    }

    DERBMPString(char[] cArray) {
        if (cArray == null) {
            throw new NullPointerException("'string' cannot be null");
        }
        this.string = cArray;
    }

    public DERBMPString(String string) {
        if (string == null) {
            throw new NullPointerException("'string' cannot be null");
        }
        this.string = string.toCharArray();
    }

    public String getString() {
        return new String(this.string);
    }

    public String toString() {
        return this.getString();
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    protected boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof DERBMPString)) {
            return false;
        }
        DERBMPString dERBMPString = (DERBMPString)aSN1Primitive;
        return Arrays.areEqual(this.string, dERBMPString.string);
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return 1 + StreamUtil.calculateBodyLength(this.string.length * 2) + this.string.length * 2;
    }

    void encode(ASN1OutputStream aSN1OutputStream, boolean bl) throws IOException {
        char c;
        int n;
        int n2;
        int n3 = this.string.length;
        if (bl) {
            aSN1OutputStream.write(30);
        }
        aSN1OutputStream.writeLength(n3 * 2);
        byte[] byArray = new byte[8];
        int n4 = n3 & 0xFFFFFFFC;
        for (n2 = 0; n2 < n4; n2 += 4) {
            n = this.string[n2];
            c = this.string[n2 + 1];
            char c2 = this.string[n2 + 2];
            char c3 = this.string[n2 + 3];
            byArray[0] = (byte)(n >> 8);
            byArray[1] = (byte)n;
            byArray[2] = (byte)(c >> 8);
            byArray[3] = (byte)c;
            byArray[4] = (byte)(c2 >> 8);
            byArray[5] = (byte)c2;
            byArray[6] = (byte)(c3 >> 8);
            byArray[7] = (byte)c3;
            aSN1OutputStream.write(byArray, 0, 8);
        }
        if (n2 < n3) {
            n = 0;
            do {
                c = this.string[n2];
                byArray[n++] = (byte)(c >> 8);
                byArray[n++] = (byte)c;
            } while (++n2 < n3);
            aSN1OutputStream.write(byArray, 0, n);
        }
    }
}

