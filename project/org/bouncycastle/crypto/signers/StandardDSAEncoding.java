/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.signers;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.signers.DSAEncoding;
import org.bouncycastle.util.Arrays;

public class StandardDSAEncoding
implements DSAEncoding {
    public static final StandardDSAEncoding INSTANCE = new StandardDSAEncoding();

    public byte[] encode(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        this.encodeValue(bigInteger, aSN1EncodableVector, bigInteger2);
        this.encodeValue(bigInteger, aSN1EncodableVector, bigInteger3);
        return new DERSequence(aSN1EncodableVector).getEncoded("DER");
    }

    public BigInteger[] decode(BigInteger bigInteger, byte[] byArray) throws IOException {
        BigInteger bigInteger2;
        BigInteger bigInteger3;
        byte[] byArray2;
        ASN1Sequence aSN1Sequence = (ASN1Sequence)ASN1Primitive.fromByteArray(byArray);
        if (aSN1Sequence.size() == 2 && Arrays.areEqual(byArray2 = this.encode(bigInteger, bigInteger3 = this.decodeValue(bigInteger, aSN1Sequence, 0), bigInteger2 = this.decodeValue(bigInteger, aSN1Sequence, 1)), byArray)) {
            return new BigInteger[]{bigInteger3, bigInteger2};
        }
        throw new IllegalArgumentException("Malformed signature");
    }

    protected BigInteger checkValue(BigInteger bigInteger, BigInteger bigInteger2) {
        if (bigInteger2.signum() < 0 || null != bigInteger && bigInteger2.compareTo(bigInteger) >= 0) {
            throw new IllegalArgumentException("Value out of range");
        }
        return bigInteger2;
    }

    protected BigInteger decodeValue(BigInteger bigInteger, ASN1Sequence aSN1Sequence, int n) {
        return this.checkValue(bigInteger, ((ASN1Integer)aSN1Sequence.getObjectAt(n)).getValue());
    }

    protected void encodeValue(BigInteger bigInteger, ASN1EncodableVector aSN1EncodableVector, BigInteger bigInteger2) {
        aSN1EncodableVector.add(new ASN1Integer(this.checkValue(bigInteger, bigInteger2)));
    }
}

