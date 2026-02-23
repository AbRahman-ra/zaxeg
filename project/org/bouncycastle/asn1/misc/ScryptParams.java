/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.misc;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Arrays;

public class ScryptParams
extends ASN1Object {
    private final byte[] salt;
    private final BigInteger costParameter;
    private final BigInteger blockSize;
    private final BigInteger parallelizationParameter;
    private final BigInteger keyLength;

    public ScryptParams(byte[] byArray, int n, int n2, int n3) {
        this(byArray, BigInteger.valueOf(n), BigInteger.valueOf(n2), BigInteger.valueOf(n3), null);
    }

    public ScryptParams(byte[] byArray, int n, int n2, int n3, int n4) {
        this(byArray, BigInteger.valueOf(n), BigInteger.valueOf(n2), BigInteger.valueOf(n3), BigInteger.valueOf(n4));
    }

    public ScryptParams(byte[] byArray, BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        this.salt = Arrays.clone(byArray);
        this.costParameter = bigInteger;
        this.blockSize = bigInteger2;
        this.parallelizationParameter = bigInteger3;
        this.keyLength = bigInteger4;
    }

    public static ScryptParams getInstance(Object object) {
        if (object instanceof ScryptParams) {
            return (ScryptParams)object;
        }
        if (object != null) {
            return new ScryptParams(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    private ScryptParams(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() != 4 && aSN1Sequence.size() != 5) {
            throw new IllegalArgumentException("invalid sequence: size = " + aSN1Sequence.size());
        }
        this.salt = Arrays.clone(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0)).getOctets());
        this.costParameter = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1)).getValue();
        this.blockSize = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(2)).getValue();
        this.parallelizationParameter = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(3)).getValue();
        this.keyLength = aSN1Sequence.size() == 5 ? ASN1Integer.getInstance(aSN1Sequence.getObjectAt(4)).getValue() : null;
    }

    public byte[] getSalt() {
        return Arrays.clone(this.salt);
    }

    public BigInteger getCostParameter() {
        return this.costParameter;
    }

    public BigInteger getBlockSize() {
        return this.blockSize;
    }

    public BigInteger getParallelizationParameter() {
        return this.parallelizationParameter;
    }

    public BigInteger getKeyLength() {
        return this.keyLength;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(5);
        aSN1EncodableVector.add(new DEROctetString(this.salt));
        aSN1EncodableVector.add(new ASN1Integer(this.costParameter));
        aSN1EncodableVector.add(new ASN1Integer(this.blockSize));
        aSN1EncodableVector.add(new ASN1Integer(this.parallelizationParameter));
        if (this.keyLength != null) {
            aSN1EncodableVector.add(new ASN1Integer(this.keyLength));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

