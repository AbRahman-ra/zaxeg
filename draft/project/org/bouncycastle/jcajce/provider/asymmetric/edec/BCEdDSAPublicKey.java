/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.KeyFactorySpi;
import org.bouncycastle.jcajce.provider.asymmetric.edec.Utils;
import org.bouncycastle.util.Arrays;

public class BCEdDSAPublicKey
implements EdDSAPublicKey {
    static final long serialVersionUID = 1L;
    transient AsymmetricKeyParameter eddsaPublicKey;

    BCEdDSAPublicKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.eddsaPublicKey = asymmetricKeyParameter;
    }

    BCEdDSAPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.populateFromPubKeyInfo(subjectPublicKeyInfo);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    BCEdDSAPublicKey(byte[] byArray, byte[] byArray2) throws InvalidKeySpecException {
        int n = byArray.length;
        if (!Utils.isValidPrefix(byArray, byArray2)) throw new InvalidKeySpecException("raw key data not recognised");
        if (byArray2.length - n == 57) {
            this.eddsaPublicKey = new Ed448PublicKeyParameters(byArray2, n);
            return;
        } else {
            if (byArray2.length - n != 32) throw new InvalidKeySpecException("raw key data not recognised");
            this.eddsaPublicKey = new Ed25519PublicKeyParameters(byArray2, n);
        }
    }

    public byte[] getPointEncoding() {
        if (this.eddsaPublicKey instanceof Ed448PublicKeyParameters) {
            return ((Ed448PublicKeyParameters)this.eddsaPublicKey).getEncoded();
        }
        return ((Ed25519PublicKeyParameters)this.eddsaPublicKey).getEncoded();
    }

    private void populateFromPubKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        byte[] byArray = subjectPublicKeyInfo.getPublicKeyData().getOctets();
        this.eddsaPublicKey = EdECObjectIdentifiers.id_Ed448.equals(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()) ? new Ed448PublicKeyParameters(byArray) : new Ed25519PublicKeyParameters(byArray);
    }

    public String getAlgorithm() {
        return this.eddsaPublicKey instanceof Ed448PublicKeyParameters ? "Ed448" : "Ed25519";
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        if (this.eddsaPublicKey instanceof Ed448PublicKeyParameters) {
            byte[] byArray = new byte[KeyFactorySpi.Ed448Prefix.length + 57];
            System.arraycopy(KeyFactorySpi.Ed448Prefix, 0, byArray, 0, KeyFactorySpi.Ed448Prefix.length);
            ((Ed448PublicKeyParameters)this.eddsaPublicKey).encode(byArray, KeyFactorySpi.Ed448Prefix.length);
            return byArray;
        }
        byte[] byArray = new byte[KeyFactorySpi.Ed25519Prefix.length + 32];
        System.arraycopy(KeyFactorySpi.Ed25519Prefix, 0, byArray, 0, KeyFactorySpi.Ed25519Prefix.length);
        ((Ed25519PublicKeyParameters)this.eddsaPublicKey).encode(byArray, KeyFactorySpi.Ed25519Prefix.length);
        return byArray;
    }

    AsymmetricKeyParameter engineGetKeyParameters() {
        return this.eddsaPublicKey;
    }

    public String toString() {
        return Utils.keyToString("Public Key", this.getAlgorithm(), this.eddsaPublicKey);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PublicKey)) {
            return false;
        }
        PublicKey publicKey = (PublicKey)object;
        return Arrays.areEqual(publicKey.getEncoded(), this.getEncoded());
    }

    public int hashCode() {
        return Arrays.hashCode(this.getEncoded());
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        byte[] byArray = (byte[])objectInputStream.readObject();
        this.populateFromPubKeyInfo(SubjectPublicKeyInfo.getInstance(byArray));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.getEncoded());
    }
}

