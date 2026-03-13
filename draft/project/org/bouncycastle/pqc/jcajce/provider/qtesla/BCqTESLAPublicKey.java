/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pqc.jcajce.provider.qtesla;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLASecurityCategory;
import org.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import org.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.pqc.jcajce.interfaces.QTESLAKey;
import org.bouncycastle.pqc.jcajce.spec.QTESLAParameterSpec;
import org.bouncycastle.util.Arrays;

public class BCqTESLAPublicKey
implements PublicKey,
QTESLAKey {
    private static final long serialVersionUID = 1L;
    private transient QTESLAPublicKeyParameters keyParams;

    public BCqTESLAPublicKey(QTESLAPublicKeyParameters qTESLAPublicKeyParameters) {
        this.keyParams = qTESLAPublicKeyParameters;
    }

    public BCqTESLAPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        this.init(subjectPublicKeyInfo);
    }

    private void init(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        this.keyParams = (QTESLAPublicKeyParameters)PublicKeyFactory.createKey(subjectPublicKeyInfo);
    }

    public final String getAlgorithm() {
        return QTESLASecurityCategory.getName(this.keyParams.getSecurityCategory());
    }

    public byte[] getEncoded() {
        try {
            SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(this.keyParams);
            return subjectPublicKeyInfo.getEncoded();
        } catch (IOException iOException) {
            return null;
        }
    }

    public String getFormat() {
        return "X.509";
    }

    public QTESLAParameterSpec getParams() {
        return new QTESLAParameterSpec(this.getAlgorithm());
    }

    CipherParameters getKeyParams() {
        return this.keyParams;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof BCqTESLAPublicKey) {
            BCqTESLAPublicKey bCqTESLAPublicKey = (BCqTESLAPublicKey)object;
            return this.keyParams.getSecurityCategory() == bCqTESLAPublicKey.keyParams.getSecurityCategory() && Arrays.areEqual(this.keyParams.getPublicData(), bCqTESLAPublicKey.keyParams.getPublicData());
        }
        return false;
    }

    public int hashCode() {
        return this.keyParams.getSecurityCategory() + 37 * Arrays.hashCode(this.keyParams.getPublicData());
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        byte[] byArray = (byte[])objectInputStream.readObject();
        this.init(SubjectPublicKeyInfo.getInstance(byArray));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.getEncoded());
    }
}

