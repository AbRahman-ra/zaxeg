/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetStringParser;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.BERTaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public abstract class ASN1OctetString
extends ASN1Primitive
implements ASN1OctetStringParser {
    byte[] string;

    public static ASN1OctetString getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        if (bl) {
            if (!aSN1TaggedObject.isExplicit()) {
                throw new IllegalArgumentException("object implicit - explicit expected.");
            }
            return ASN1OctetString.getInstance(aSN1TaggedObject.getObject());
        }
        ASN1Primitive aSN1Primitive = aSN1TaggedObject.getObject();
        if (aSN1TaggedObject.isExplicit()) {
            ASN1OctetString aSN1OctetString = ASN1OctetString.getInstance(aSN1Primitive);
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return new BEROctetString(new ASN1OctetString[]{aSN1OctetString});
            }
            return (ASN1OctetString)new BEROctetString(new ASN1OctetString[]{aSN1OctetString}).toDLObject();
        }
        if (aSN1Primitive instanceof ASN1OctetString) {
            ASN1OctetString aSN1OctetString = (ASN1OctetString)aSN1Primitive;
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return aSN1OctetString;
            }
            return (ASN1OctetString)aSN1OctetString.toDLObject();
        }
        if (aSN1Primitive instanceof ASN1Sequence) {
            ASN1Sequence aSN1Sequence = (ASN1Sequence)aSN1Primitive;
            if (aSN1TaggedObject instanceof BERTaggedObject) {
                return BEROctetString.fromSequence(aSN1Sequence);
            }
            return (ASN1OctetString)BEROctetString.fromSequence(aSN1Sequence).toDLObject();
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + aSN1TaggedObject.getClass().getName());
    }

    public static ASN1OctetString getInstance(Object object) {
        ASN1Primitive aSN1Primitive;
        if (object == null || object instanceof ASN1OctetString) {
            return (ASN1OctetString)object;
        }
        if (object instanceof byte[]) {
            try {
                return ASN1OctetString.getInstance(ASN1OctetString.fromByteArray((byte[])object));
            } catch (IOException iOException) {
                throw new IllegalArgumentException("failed to construct OCTET STRING from byte[]: " + iOException.getMessage());
            }
        }
        if (object instanceof ASN1Encodable && (aSN1Primitive = ((ASN1Encodable)object).toASN1Primitive()) instanceof ASN1OctetString) {
            return (ASN1OctetString)aSN1Primitive;
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + object.getClass().getName());
    }

    public ASN1OctetString(byte[] byArray) {
        if (byArray == null) {
            throw new NullPointerException("'string' cannot be null");
        }
        this.string = byArray;
    }

    public InputStream getOctetStream() {
        return new ByteArrayInputStream(this.string);
    }

    public ASN1OctetStringParser parser() {
        return this;
    }

    public byte[] getOctets() {
        return this.string;
    }

    public int hashCode() {
        return Arrays.hashCode(this.getOctets());
    }

    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1OctetString)) {
            return false;
        }
        ASN1OctetString aSN1OctetString = (ASN1OctetString)aSN1Primitive;
        return Arrays.areEqual(this.string, aSN1OctetString.string);
    }

    public ASN1Primitive getLoadedObject() {
        return this.toASN1Primitive();
    }

    ASN1Primitive toDERObject() {
        return new DEROctetString(this.string);
    }

    ASN1Primitive toDLObject() {
        return new DEROctetString(this.string);
    }

    abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

    public String toString() {
        return "#" + Strings.fromByteArray(Hex.encode(this.string));
    }
}

