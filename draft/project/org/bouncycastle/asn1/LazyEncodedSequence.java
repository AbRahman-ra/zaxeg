/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.LazyConstructionEnumeration;
import org.bouncycastle.asn1.StreamUtil;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class LazyEncodedSequence
extends ASN1Sequence {
    private byte[] encoded;

    LazyEncodedSequence(byte[] byArray) throws IOException {
        this.encoded = byArray;
    }

    @Override
    public synchronized ASN1Encodable getObjectAt(int n) {
        this.force();
        return super.getObjectAt(n);
    }

    @Override
    public synchronized Enumeration getObjects() {
        if (null != this.encoded) {
            return new LazyConstructionEnumeration(this.encoded);
        }
        return super.getObjects();
    }

    @Override
    public synchronized int hashCode() {
        this.force();
        return super.hashCode();
    }

    @Override
    public synchronized Iterator<ASN1Encodable> iterator() {
        this.force();
        return super.iterator();
    }

    @Override
    public synchronized int size() {
        this.force();
        return super.size();
    }

    @Override
    public synchronized ASN1Encodable[] toArray() {
        this.force();
        return super.toArray();
    }

    @Override
    ASN1Encodable[] toArrayInternal() {
        this.force();
        return super.toArrayInternal();
    }

    @Override
    synchronized int encodedLength() throws IOException {
        if (null != this.encoded) {
            return 1 + StreamUtil.calculateBodyLength(this.encoded.length) + this.encoded.length;
        }
        return super.toDLObject().encodedLength();
    }

    @Override
    synchronized void encode(ASN1OutputStream aSN1OutputStream, boolean bl) throws IOException {
        if (null != this.encoded) {
            aSN1OutputStream.writeEncoded(bl, 48, this.encoded);
        } else {
            super.toDLObject().encode(aSN1OutputStream, bl);
        }
    }

    @Override
    synchronized ASN1Primitive toDERObject() {
        this.force();
        return super.toDERObject();
    }

    @Override
    synchronized ASN1Primitive toDLObject() {
        this.force();
        return super.toDLObject();
    }

    private void force() {
        if (null != this.encoded) {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            LazyConstructionEnumeration lazyConstructionEnumeration = new LazyConstructionEnumeration(this.encoded);
            while (lazyConstructionEnumeration.hasMoreElements()) {
                aSN1EncodableVector.add((ASN1Primitive)lazyConstructionEnumeration.nextElement());
            }
            this.elements = aSN1EncodableVector.takeElements();
            this.encoded = null;
        }
    }
}

