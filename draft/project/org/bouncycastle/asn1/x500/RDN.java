/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x500;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;

public class RDN
extends ASN1Object {
    private ASN1Set values;

    private RDN(ASN1Set aSN1Set) {
        this.values = aSN1Set;
    }

    public static RDN getInstance(Object object) {
        if (object instanceof RDN) {
            return (RDN)object;
        }
        if (object != null) {
            return new RDN(ASN1Set.getInstance(object));
        }
        return null;
    }

    public RDN(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(aSN1ObjectIdentifier);
        aSN1EncodableVector.add(aSN1Encodable);
        this.values = new DERSet(new DERSequence(aSN1EncodableVector));
    }

    public RDN(AttributeTypeAndValue attributeTypeAndValue) {
        this.values = new DERSet(attributeTypeAndValue);
    }

    public RDN(AttributeTypeAndValue[] attributeTypeAndValueArray) {
        this.values = new DERSet(attributeTypeAndValueArray);
    }

    public boolean isMultiValued() {
        return this.values.size() > 1;
    }

    public int size() {
        return this.values.size();
    }

    public AttributeTypeAndValue getFirst() {
        if (this.values.size() == 0) {
            return null;
        }
        return AttributeTypeAndValue.getInstance(this.values.getObjectAt(0));
    }

    public AttributeTypeAndValue[] getTypesAndValues() {
        AttributeTypeAndValue[] attributeTypeAndValueArray = new AttributeTypeAndValue[this.values.size()];
        for (int i = 0; i != attributeTypeAndValueArray.length; ++i) {
            attributeTypeAndValueArray[i] = AttributeTypeAndValue.getInstance(this.values.getObjectAt(i));
        }
        return attributeTypeAndValueArray;
    }

    int collectAttributeTypes(ASN1ObjectIdentifier[] aSN1ObjectIdentifierArray, int n) {
        int n2 = this.values.size();
        for (int i = 0; i < n2; ++i) {
            AttributeTypeAndValue attributeTypeAndValue = AttributeTypeAndValue.getInstance(this.values.getObjectAt(i));
            aSN1ObjectIdentifierArray[n + i] = attributeTypeAndValue.getType();
        }
        return n2;
    }

    boolean containsAttributeType(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        int n = this.values.size();
        for (int i = 0; i < n; ++i) {
            AttributeTypeAndValue attributeTypeAndValue = AttributeTypeAndValue.getInstance(this.values.getObjectAt(i));
            if (!attributeTypeAndValue.getType().equals(aSN1ObjectIdentifier)) continue;
            return true;
        }
        return false;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.values;
    }
}

