/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERExternal;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLExternal;

public abstract class ASN1External
extends ASN1Primitive {
    protected ASN1ObjectIdentifier directReference;
    protected ASN1Integer indirectReference;
    protected ASN1Primitive dataValueDescriptor;
    protected int encoding;
    protected ASN1Primitive externalContent;

    public ASN1External(ASN1EncodableVector aSN1EncodableVector) {
        int n = 0;
        ASN1Primitive aSN1Primitive = this.getObjFromVector(aSN1EncodableVector, n);
        if (aSN1Primitive instanceof ASN1ObjectIdentifier) {
            this.directReference = (ASN1ObjectIdentifier)aSN1Primitive;
            aSN1Primitive = this.getObjFromVector(aSN1EncodableVector, ++n);
        }
        if (aSN1Primitive instanceof ASN1Integer) {
            this.indirectReference = (ASN1Integer)aSN1Primitive;
            aSN1Primitive = this.getObjFromVector(aSN1EncodableVector, ++n);
        }
        if (!(aSN1Primitive instanceof ASN1TaggedObject)) {
            this.dataValueDescriptor = aSN1Primitive;
            aSN1Primitive = this.getObjFromVector(aSN1EncodableVector, ++n);
        }
        if (aSN1EncodableVector.size() != n + 1) {
            throw new IllegalArgumentException("input vector too large");
        }
        if (!(aSN1Primitive instanceof ASN1TaggedObject)) {
            throw new IllegalArgumentException("No tagged object found in vector. Structure doesn't seem to be of type External");
        }
        ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject)aSN1Primitive;
        this.setEncoding(aSN1TaggedObject.getTagNo());
        this.externalContent = aSN1TaggedObject.getObject();
    }

    private ASN1Primitive getObjFromVector(ASN1EncodableVector aSN1EncodableVector, int n) {
        if (aSN1EncodableVector.size() <= n) {
            throw new IllegalArgumentException("too few objects in input vector");
        }
        return aSN1EncodableVector.get(n).toASN1Primitive();
    }

    public ASN1External(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Integer aSN1Integer, ASN1Primitive aSN1Primitive, DERTaggedObject dERTaggedObject) {
        this(aSN1ObjectIdentifier, aSN1Integer, aSN1Primitive, dERTaggedObject.getTagNo(), dERTaggedObject.toASN1Primitive());
    }

    public ASN1External(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Integer aSN1Integer, ASN1Primitive aSN1Primitive, int n, ASN1Primitive aSN1Primitive2) {
        this.setDirectReference(aSN1ObjectIdentifier);
        this.setIndirectReference(aSN1Integer);
        this.setDataValueDescriptor(aSN1Primitive);
        this.setEncoding(n);
        this.setExternalContent(aSN1Primitive2.toASN1Primitive());
    }

    ASN1Primitive toDERObject() {
        return new DERExternal(this.directReference, this.indirectReference, this.dataValueDescriptor, this.encoding, this.externalContent);
    }

    ASN1Primitive toDLObject() {
        return new DLExternal(this.directReference, this.indirectReference, this.dataValueDescriptor, this.encoding, this.externalContent);
    }

    public int hashCode() {
        int n = 0;
        if (this.directReference != null) {
            n = this.directReference.hashCode();
        }
        if (this.indirectReference != null) {
            n ^= this.indirectReference.hashCode();
        }
        if (this.dataValueDescriptor != null) {
            n ^= this.dataValueDescriptor.hashCode();
        }
        return n ^= this.externalContent.hashCode();
    }

    boolean isConstructed() {
        return true;
    }

    int encodedLength() throws IOException {
        return this.getEncoded().length;
    }

    boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1External)) {
            return false;
        }
        if (this == aSN1Primitive) {
            return true;
        }
        ASN1External aSN1External = (ASN1External)aSN1Primitive;
        if (!(this.directReference == null || aSN1External.directReference != null && aSN1External.directReference.equals(this.directReference))) {
            return false;
        }
        if (!(this.indirectReference == null || aSN1External.indirectReference != null && aSN1External.indirectReference.equals(this.indirectReference))) {
            return false;
        }
        if (!(this.dataValueDescriptor == null || aSN1External.dataValueDescriptor != null && aSN1External.dataValueDescriptor.equals(this.dataValueDescriptor))) {
            return false;
        }
        return this.externalContent.equals(aSN1External.externalContent);
    }

    public ASN1Primitive getDataValueDescriptor() {
        return this.dataValueDescriptor;
    }

    public ASN1ObjectIdentifier getDirectReference() {
        return this.directReference;
    }

    public int getEncoding() {
        return this.encoding;
    }

    public ASN1Primitive getExternalContent() {
        return this.externalContent;
    }

    public ASN1Integer getIndirectReference() {
        return this.indirectReference;
    }

    private void setDataValueDescriptor(ASN1Primitive aSN1Primitive) {
        this.dataValueDescriptor = aSN1Primitive;
    }

    private void setDirectReference(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.directReference = aSN1ObjectIdentifier;
    }

    private void setEncoding(int n) {
        if (n < 0 || n > 2) {
            throw new IllegalArgumentException("invalid encoding value: " + n);
        }
        this.encoding = n;
    }

    private void setExternalContent(ASN1Primitive aSN1Primitive) {
        this.externalContent = aSN1Primitive;
    }

    private void setIndirectReference(ASN1Integer aSN1Integer) {
        this.indirectReference = aSN1Integer;
    }
}

