/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OutputStream;

public abstract class ASN1Primitive
extends ASN1Object {
    ASN1Primitive() {
    }

    public void encodeTo(OutputStream outputStream) throws IOException {
        ASN1OutputStream.create(outputStream).writeObject(this);
    }

    public void encodeTo(OutputStream outputStream, String string) throws IOException {
        ASN1OutputStream.create(outputStream, string).writeObject(this);
    }

    public static ASN1Primitive fromByteArray(byte[] byArray) throws IOException {
        ASN1InputStream aSN1InputStream = new ASN1InputStream(byArray);
        try {
            ASN1Primitive aSN1Primitive = aSN1InputStream.readObject();
            if (aSN1InputStream.available() != 0) {
                throw new IOException("Extra data detected in stream");
            }
            return aSN1Primitive;
        } catch (ClassCastException classCastException) {
            throw new IOException("cannot recognise object in stream");
        }
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ASN1Encodable && this.asn1Equals(((ASN1Encodable)object).toASN1Primitive());
    }

    public final boolean equals(ASN1Encodable aSN1Encodable) {
        return this == aSN1Encodable || null != aSN1Encodable && this.asn1Equals(aSN1Encodable.toASN1Primitive());
    }

    public final boolean equals(ASN1Primitive aSN1Primitive) {
        return this == aSN1Primitive || this.asn1Equals(aSN1Primitive);
    }

    public final ASN1Primitive toASN1Primitive() {
        return this;
    }

    ASN1Primitive toDERObject() {
        return this;
    }

    ASN1Primitive toDLObject() {
        return this;
    }

    public abstract int hashCode();

    abstract boolean isConstructed();

    abstract int encodedLength() throws IOException;

    abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

    abstract boolean asn1Equals(ASN1Primitive var1);
}

