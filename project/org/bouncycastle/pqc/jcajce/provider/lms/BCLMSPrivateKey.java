/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.jcajce.provider.lms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import org.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.pqc.jcajce.interfaces.LMSPrivateKey;
import org.bouncycastle.util.Arrays;

public class BCLMSPrivateKey
implements PrivateKey,
LMSPrivateKey {
    private static final long serialVersionUID = 8568701712864512338L;
    private transient LMSKeyParameters keyParams;
    private transient ASN1Set attributes;

    public BCLMSPrivateKey(LMSKeyParameters lMSKeyParameters) {
        this.keyParams = lMSKeyParameters;
    }

    public BCLMSPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.init(privateKeyInfo);
    }

    private void init(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.attributes = privateKeyInfo.getAttributes();
        this.keyParams = (LMSKeyParameters)PrivateKeyFactory.createKey(privateKeyInfo);
    }

    public long getIndex() {
        if (this.getUsagesRemaining() == 0L) {
            throw new IllegalStateException("key exhausted");
        }
        if (this.keyParams instanceof LMSPrivateKeyParameters) {
            return ((LMSPrivateKeyParameters)this.keyParams).getIndex();
        }
        return ((HSSPrivateKeyParameters)this.keyParams).getIndex();
    }

    public long getUsagesRemaining() {
        if (this.keyParams instanceof LMSPrivateKeyParameters) {
            return ((LMSPrivateKeyParameters)this.keyParams).getUsagesRemaining();
        }
        return ((HSSPrivateKeyParameters)this.keyParams).getUsagesRemaining();
    }

    public LMSPrivateKey extractKeyShard(int n) {
        if (this.keyParams instanceof LMSPrivateKeyParameters) {
            return new BCLMSPrivateKey(((LMSPrivateKeyParameters)this.keyParams).extractKeyShard(n));
        }
        return new BCLMSPrivateKey(((HSSPrivateKeyParameters)this.keyParams).extractKeyShard(n));
    }

    public String getAlgorithm() {
        return "LMS";
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        try {
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(this.keyParams, this.attributes);
            return privateKeyInfo.getEncoded();
        } catch (IOException iOException) {
            return null;
        }
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof BCLMSPrivateKey) {
            BCLMSPrivateKey bCLMSPrivateKey = (BCLMSPrivateKey)object;
            try {
                return Arrays.areEqual(this.keyParams.getEncoded(), bCLMSPrivateKey.keyParams.getEncoded());
            } catch (IOException iOException) {
                throw new IllegalStateException("unable to perform equals");
            }
        }
        return false;
    }

    public int hashCode() {
        try {
            return Arrays.hashCode(this.keyParams.getEncoded());
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to calculate hashCode");
        }
    }

    CipherParameters getKeyParams() {
        return this.keyParams;
    }

    public int getLevels() {
        if (this.keyParams instanceof LMSPrivateKeyParameters) {
            return 1;
        }
        return ((HSSPrivateKeyParameters)this.keyParams).getL();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        byte[] byArray = (byte[])objectInputStream.readObject();
        this.init(PrivateKeyInfo.getInstance(byArray));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.getEncoded());
    }
}

