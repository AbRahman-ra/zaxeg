/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.bc;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.util.Arrays;

public class SignatureCheck
extends ASN1Object {
    private final AlgorithmIdentifier signatureAlgorithm;
    private final ASN1Sequence certificates;
    private final ASN1BitString signatureValue;

    public SignatureCheck(AlgorithmIdentifier algorithmIdentifier, byte[] byArray) {
        this.signatureAlgorithm = algorithmIdentifier;
        this.certificates = null;
        this.signatureValue = new DERBitString(Arrays.clone(byArray));
    }

    public SignatureCheck(AlgorithmIdentifier algorithmIdentifier, Certificate[] certificateArray, byte[] byArray) {
        this.signatureAlgorithm = algorithmIdentifier;
        this.certificates = new DERSequence(certificateArray);
        this.signatureValue = new DERBitString(Arrays.clone(byArray));
    }

    private SignatureCheck(ASN1Sequence aSN1Sequence) {
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        int n = 1;
        this.certificates = aSN1Sequence.getObjectAt(1) instanceof ASN1TaggedObject ? ASN1Sequence.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(n++)).getObject()) : null;
        this.signatureValue = DERBitString.getInstance(aSN1Sequence.getObjectAt(n));
    }

    public static SignatureCheck getInstance(Object object) {
        if (object instanceof SignatureCheck) {
            return (SignatureCheck)object;
        }
        if (object != null) {
            return new SignatureCheck(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    public ASN1BitString getSignature() {
        return new DERBitString(this.signatureValue.getBytes(), this.signatureValue.getPadBits());
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    public Certificate[] getCertificates() {
        if (this.certificates == null) {
            return null;
        }
        Certificate[] certificateArray = new Certificate[this.certificates.size()];
        for (int i = 0; i != certificateArray.length; ++i) {
            certificateArray[i] = Certificate.getInstance(this.certificates.getObjectAt(i));
        }
        return certificateArray;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        aSN1EncodableVector.add(this.signatureAlgorithm);
        if (this.certificates != null) {
            aSN1EncodableVector.add(new DERTaggedObject(0, this.certificates));
        }
        aSN1EncodableVector.add(this.signatureValue);
        return new DERSequence(aSN1EncodableVector);
    }
}

