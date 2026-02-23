/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.pkcs;

import java.io.IOException;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.BigIntegers;

public class PrivateKeyInfo
extends ASN1Object {
    private ASN1Integer version;
    private AlgorithmIdentifier privateKeyAlgorithm;
    private ASN1OctetString privateKey;
    private ASN1Set attributes;
    private ASN1BitString publicKey;

    public static PrivateKeyInfo getInstance(ASN1TaggedObject aSN1TaggedObject, boolean bl) {
        return PrivateKeyInfo.getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, bl));
    }

    public static PrivateKeyInfo getInstance(Object object) {
        if (object instanceof PrivateKeyInfo) {
            return (PrivateKeyInfo)object;
        }
        if (object != null) {
            return new PrivateKeyInfo(ASN1Sequence.getInstance(object));
        }
        return null;
    }

    private static int getVersionValue(ASN1Integer aSN1Integer) {
        int n = aSN1Integer.intValueExact();
        if (n < 0 || n > 1) {
            throw new IllegalArgumentException("invalid version for private key info");
        }
        return n;
    }

    public PrivateKeyInfo(AlgorithmIdentifier algorithmIdentifier, ASN1Encodable aSN1Encodable) throws IOException {
        this(algorithmIdentifier, aSN1Encodable, null, null);
    }

    public PrivateKeyInfo(AlgorithmIdentifier algorithmIdentifier, ASN1Encodable aSN1Encodable, ASN1Set aSN1Set) throws IOException {
        this(algorithmIdentifier, aSN1Encodable, aSN1Set, null);
    }

    public PrivateKeyInfo(AlgorithmIdentifier algorithmIdentifier, ASN1Encodable aSN1Encodable, ASN1Set aSN1Set, byte[] byArray) throws IOException {
        this.version = new ASN1Integer(byArray != null ? BigIntegers.ONE : BigIntegers.ZERO);
        this.privateKeyAlgorithm = algorithmIdentifier;
        this.privateKey = new DEROctetString(aSN1Encodable);
        this.attributes = aSN1Set;
        this.publicKey = byArray == null ? null : new DERBitString(byArray);
    }

    private PrivateKeyInfo(ASN1Sequence aSN1Sequence) {
        Enumeration enumeration = aSN1Sequence.getObjects();
        this.version = ASN1Integer.getInstance(enumeration.nextElement());
        int n = PrivateKeyInfo.getVersionValue(this.version);
        this.privateKeyAlgorithm = AlgorithmIdentifier.getInstance(enumeration.nextElement());
        this.privateKey = ASN1OctetString.getInstance(enumeration.nextElement());
        int n2 = -1;
        block4: while (enumeration.hasMoreElements()) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject)enumeration.nextElement();
            int n3 = aSN1TaggedObject.getTagNo();
            if (n3 <= n2) {
                throw new IllegalArgumentException("invalid optional field in private key info");
            }
            n2 = n3;
            switch (n3) {
                case 0: {
                    this.attributes = ASN1Set.getInstance(aSN1TaggedObject, false);
                    continue block4;
                }
                case 1: {
                    if (n < 1) {
                        throw new IllegalArgumentException("'publicKey' requires version v2(1) or later");
                    }
                    this.publicKey = DERBitString.getInstance(aSN1TaggedObject, false);
                    continue block4;
                }
            }
            throw new IllegalArgumentException("unknown optional field in private key info");
        }
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Set getAttributes() {
        return this.attributes;
    }

    public AlgorithmIdentifier getPrivateKeyAlgorithm() {
        return this.privateKeyAlgorithm;
    }

    public ASN1OctetString getPrivateKey() {
        return new DEROctetString(this.privateKey.getOctets());
    }

    public ASN1Encodable parsePrivateKey() throws IOException {
        return ASN1Primitive.fromByteArray(this.privateKey.getOctets());
    }

    public boolean hasPublicKey() {
        return this.publicKey != null;
    }

    public ASN1Encodable parsePublicKey() throws IOException {
        return this.publicKey == null ? null : ASN1Primitive.fromByteArray(this.publicKey.getOctets());
    }

    public ASN1BitString getPublicKeyData() {
        return this.publicKey;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(5);
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.privateKeyAlgorithm);
        aSN1EncodableVector.add(this.privateKey);
        if (this.attributes != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.attributes));
        }
        if (this.publicKey != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.publicKey));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

