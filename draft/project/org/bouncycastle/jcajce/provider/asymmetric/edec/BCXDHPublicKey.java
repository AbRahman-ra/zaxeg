/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X448PublicKeyParameters;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.KeyFactorySpi;
import org.bouncycastle.jcajce.provider.asymmetric.edec.Utils;
import org.bouncycastle.util.Arrays;

public class BCXDHPublicKey
implements XDHPublicKey {
    static final long serialVersionUID = 1L;
    transient AsymmetricKeyParameter xdhPublicKey;

    BCXDHPublicKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.xdhPublicKey = asymmetricKeyParameter;
    }

    BCXDHPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.populateFromPubKeyInfo(subjectPublicKeyInfo);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    BCXDHPublicKey(byte[] byArray, byte[] byArray2) throws InvalidKeySpecException {
        int n = byArray.length;
        if (!Utils.isValidPrefix(byArray, byArray2)) throw new InvalidKeySpecException("raw key data not recognised");
        if (byArray2.length - n == 56) {
            this.xdhPublicKey = new X448PublicKeyParameters(byArray2, n);
            return;
        } else {
            if (byArray2.length - n != 32) throw new InvalidKeySpecException("raw key data not recognised");
            this.xdhPublicKey = new X25519PublicKeyParameters(byArray2, n);
        }
    }

    private void populateFromPubKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        byte[] byArray = subjectPublicKeyInfo.getPublicKeyData().getOctets();
        this.xdhPublicKey = EdECObjectIdentifiers.id_X448.equals(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()) ? new X448PublicKeyParameters(byArray) : new X25519PublicKeyParameters(byArray);
    }

    public String getAlgorithm() {
        return this.xdhPublicKey instanceof X448PublicKeyParameters ? "X448" : "X25519";
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        if (this.xdhPublicKey instanceof X448PublicKeyParameters) {
            byte[] byArray = new byte[KeyFactorySpi.x448Prefix.length + 56];
            System.arraycopy(KeyFactorySpi.x448Prefix, 0, byArray, 0, KeyFactorySpi.x448Prefix.length);
            ((X448PublicKeyParameters)this.xdhPublicKey).encode(byArray, KeyFactorySpi.x448Prefix.length);
            return byArray;
        }
        byte[] byArray = new byte[KeyFactorySpi.x25519Prefix.length + 32];
        System.arraycopy(KeyFactorySpi.x25519Prefix, 0, byArray, 0, KeyFactorySpi.x25519Prefix.length);
        ((X25519PublicKeyParameters)this.xdhPublicKey).encode(byArray, KeyFactorySpi.x25519Prefix.length);
        return byArray;
    }

    AsymmetricKeyParameter engineGetKeyParameters() {
        return this.xdhPublicKey;
    }

    public BigInteger getU() {
        byte[] byArray = this.getUEncoding();
        Arrays.reverseInPlace(byArray);
        return new BigInteger(1, byArray);
    }

    public byte[] getUEncoding() {
        if (this.xdhPublicKey instanceof X448PublicKeyParameters) {
            return ((X448PublicKeyParameters)this.xdhPublicKey).getEncoded();
        }
        return ((X25519PublicKeyParameters)this.xdhPublicKey).getEncoded();
    }

    public String toString() {
        return Utils.keyToString("Public Key", this.getAlgorithm(), this.xdhPublicKey);
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

