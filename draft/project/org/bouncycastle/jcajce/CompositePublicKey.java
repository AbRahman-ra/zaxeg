/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CompositePublicKey
implements PublicKey {
    private final List<PublicKey> keys;

    public CompositePublicKey(PublicKey ... publicKeyArray) {
        if (publicKeyArray == null || publicKeyArray.length == 0) {
            throw new IllegalArgumentException("at least one public key must be provided");
        }
        ArrayList<PublicKey> arrayList = new ArrayList<PublicKey>(publicKeyArray.length);
        for (int i = 0; i != publicKeyArray.length; ++i) {
            arrayList.add(publicKeyArray[i]);
        }
        this.keys = Collections.unmodifiableList(arrayList);
    }

    public List<PublicKey> getPublicKeys() {
        return this.keys;
    }

    @Override
    public String getAlgorithm() {
        return "Composite";
    }

    @Override
    public String getFormat() {
        return "X.509";
    }

    @Override
    public byte[] getEncoded() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (int i = 0; i != this.keys.size(); ++i) {
            aSN1EncodableVector.add(SubjectPublicKeyInfo.getInstance(this.keys.get(i).getEncoded()));
        }
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(MiscObjectIdentifiers.id_alg_composite), new DERSequence(aSN1EncodableVector)).getEncoded("DER");
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to encode composite key: " + iOException.getMessage());
        }
    }

    public int hashCode() {
        return this.keys.hashCode();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CompositePublicKey) {
            return this.keys.equals(((CompositePublicKey)object).keys);
        }
        return false;
    }
}

