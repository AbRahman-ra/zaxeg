/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CompositePrivateKey
implements PrivateKey {
    private final List<PrivateKey> keys;

    public CompositePrivateKey(PrivateKey ... privateKeyArray) {
        if (privateKeyArray == null || privateKeyArray.length == 0) {
            throw new IllegalArgumentException("at least one public key must be provided");
        }
        ArrayList<PrivateKey> arrayList = new ArrayList<PrivateKey>(privateKeyArray.length);
        for (int i = 0; i != privateKeyArray.length; ++i) {
            arrayList.add(privateKeyArray[i]);
        }
        this.keys = Collections.unmodifiableList(arrayList);
    }

    public List<PrivateKey> getPrivateKeys() {
        return this.keys;
    }

    @Override
    public String getAlgorithm() {
        return "Composite";
    }

    @Override
    public String getFormat() {
        return "PKCS#8";
    }

    @Override
    public byte[] getEncoded() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (int i = 0; i != this.keys.size(); ++i) {
            aSN1EncodableVector.add(PrivateKeyInfo.getInstance(this.keys.get(i).getEncoded()));
        }
        try {
            return new PrivateKeyInfo(new AlgorithmIdentifier(MiscObjectIdentifiers.id_alg_composite), new DERSequence(aSN1EncodableVector)).getEncoded("DER");
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
        if (object instanceof CompositePrivateKey) {
            return this.keys.equals(((CompositePrivateKey)object).keys);
        }
        return false;
    }
}

