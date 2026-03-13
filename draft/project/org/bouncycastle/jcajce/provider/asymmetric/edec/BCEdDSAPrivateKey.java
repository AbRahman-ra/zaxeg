/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.Utils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Properties;

public class BCEdDSAPrivateKey
implements EdDSAPrivateKey {
    static final long serialVersionUID = 1L;
    transient AsymmetricKeyParameter eddsaPrivateKey;
    private final boolean hasPublicKey;
    private final byte[] attributes;

    BCEdDSAPrivateKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.hasPublicKey = true;
        this.attributes = null;
        this.eddsaPrivateKey = asymmetricKeyParameter;
    }

    BCEdDSAPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.hasPublicKey = privateKeyInfo.hasPublicKey();
        this.attributes = privateKeyInfo.getAttributes() != null ? privateKeyInfo.getAttributes().getEncoded() : null;
        this.populateFromPrivateKeyInfo(privateKeyInfo);
    }

    private void populateFromPrivateKeyInfo(PrivateKeyInfo privateKeyInfo) throws IOException {
        byte[] byArray = ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets();
        this.eddsaPrivateKey = EdECObjectIdentifiers.id_Ed448.equals(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm()) ? new Ed448PrivateKeyParameters(byArray) : new Ed25519PrivateKeyParameters(byArray);
    }

    public String getAlgorithm() {
        return this.eddsaPrivateKey instanceof Ed448PrivateKeyParameters ? "Ed448" : "Ed25519";
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        try {
            ASN1Set aSN1Set = ASN1Set.getInstance(this.attributes);
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(this.eddsaPrivateKey, aSN1Set);
            if (this.hasPublicKey && !Properties.isOverrideSet("org.bouncycastle.pkcs8.v1_info_only")) {
                return privateKeyInfo.getEncoded();
            }
            return new PrivateKeyInfo(privateKeyInfo.getPrivateKeyAlgorithm(), privateKeyInfo.parsePrivateKey(), aSN1Set).getEncoded();
        } catch (IOException iOException) {
            return null;
        }
    }

    public EdDSAPublicKey getPublicKey() {
        if (this.eddsaPrivateKey instanceof Ed448PrivateKeyParameters) {
            return new BCEdDSAPublicKey(((Ed448PrivateKeyParameters)this.eddsaPrivateKey).generatePublicKey());
        }
        return new BCEdDSAPublicKey(((Ed25519PrivateKeyParameters)this.eddsaPrivateKey).generatePublicKey());
    }

    AsymmetricKeyParameter engineGetKeyParameters() {
        return this.eddsaPrivateKey;
    }

    public String toString() {
        AsymmetricKeyParameter asymmetricKeyParameter = this.eddsaPrivateKey instanceof Ed448PrivateKeyParameters ? ((Ed448PrivateKeyParameters)this.eddsaPrivateKey).generatePublicKey() : ((Ed25519PrivateKeyParameters)this.eddsaPrivateKey).generatePublicKey();
        return Utils.keyToString("Private Key", this.getAlgorithm(), asymmetricKeyParameter);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PrivateKey)) {
            return false;
        }
        PrivateKey privateKey = (PrivateKey)object;
        return Arrays.areEqual(privateKey.getEncoded(), this.getEncoded());
    }

    public int hashCode() {
        return Arrays.hashCode(this.getEncoded());
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        byte[] byArray = (byte[])objectInputStream.readObject();
        this.populateFromPrivateKeyInfo(PrivateKeyInfo.getInstance(byArray));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.getEncoded());
    }
}

