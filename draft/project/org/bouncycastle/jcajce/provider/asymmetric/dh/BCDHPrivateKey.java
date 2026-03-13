/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.asymmetric.dh;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPrivateKeySpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.DomainParameters;
import org.bouncycastle.asn1.x9.ValidationParams;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;
import org.bouncycastle.jcajce.provider.asymmetric.dh.DHUtil;
import org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import org.bouncycastle.jcajce.spec.DHDomainParameterSpec;
import org.bouncycastle.jcajce.spec.DHExtendedPrivateKeySpec;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;

public class BCDHPrivateKey
implements DHPrivateKey,
PKCS12BagAttributeCarrier {
    static final long serialVersionUID = 311058815616901812L;
    private BigInteger x;
    private transient DHParameterSpec dhSpec;
    private transient PrivateKeyInfo info;
    private transient DHPrivateKeyParameters dhPrivateKey;
    private transient PKCS12BagAttributeCarrierImpl attrCarrier = new PKCS12BagAttributeCarrierImpl();

    protected BCDHPrivateKey() {
    }

    BCDHPrivateKey(DHPrivateKey dHPrivateKey) {
        this.x = dHPrivateKey.getX();
        this.dhSpec = dHPrivateKey.getParams();
    }

    BCDHPrivateKey(DHPrivateKeySpec dHPrivateKeySpec) {
        this.x = dHPrivateKeySpec.getX();
        this.dhSpec = dHPrivateKeySpec instanceof DHExtendedPrivateKeySpec ? ((DHExtendedPrivateKeySpec)dHPrivateKeySpec).getParams() : new DHParameterSpec(dHPrivateKeySpec.getP(), dHPrivateKeySpec.getG());
    }

    public BCDHPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
        ASN1Integer aSN1Integer = (ASN1Integer)privateKeyInfo.parsePrivateKey();
        ASN1ObjectIdentifier aSN1ObjectIdentifier = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm();
        this.info = privateKeyInfo;
        this.x = aSN1Integer.getValue();
        if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.dhKeyAgreement)) {
            DHParameter dHParameter = DHParameter.getInstance(aSN1Sequence);
            if (dHParameter.getL() != null) {
                this.dhSpec = new DHParameterSpec(dHParameter.getP(), dHParameter.getG(), dHParameter.getL().intValue());
                this.dhPrivateKey = new DHPrivateKeyParameters(this.x, new DHParameters(dHParameter.getP(), dHParameter.getG(), null, dHParameter.getL().intValue()));
            } else {
                this.dhSpec = new DHParameterSpec(dHParameter.getP(), dHParameter.getG());
                this.dhPrivateKey = new DHPrivateKeyParameters(this.x, new DHParameters(dHParameter.getP(), dHParameter.getG()));
            }
        } else if (aSN1ObjectIdentifier.equals(X9ObjectIdentifiers.dhpublicnumber)) {
            DomainParameters domainParameters = DomainParameters.getInstance(aSN1Sequence);
            this.dhSpec = new DHDomainParameterSpec(domainParameters.getP(), domainParameters.getQ(), domainParameters.getG(), domainParameters.getJ(), 0);
            this.dhPrivateKey = new DHPrivateKeyParameters(this.x, new DHParameters(domainParameters.getP(), domainParameters.getG(), domainParameters.getQ(), domainParameters.getJ(), null));
        } else {
            throw new IllegalArgumentException("unknown algorithm type: " + aSN1ObjectIdentifier);
        }
    }

    BCDHPrivateKey(DHPrivateKeyParameters dHPrivateKeyParameters) {
        this.x = dHPrivateKeyParameters.getX();
        this.dhSpec = new DHDomainParameterSpec(dHPrivateKeyParameters.getParameters());
    }

    public String getAlgorithm() {
        return "DH";
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        try {
            PrivateKeyInfo privateKeyInfo;
            if (this.info != null) {
                return this.info.getEncoded("DER");
            }
            if (this.dhSpec instanceof DHDomainParameterSpec && ((DHDomainParameterSpec)this.dhSpec).getQ() != null) {
                DHParameters dHParameters = ((DHDomainParameterSpec)this.dhSpec).getDomainParameters();
                DHValidationParameters dHValidationParameters = dHParameters.getValidationParameters();
                ValidationParams validationParams = null;
                if (dHValidationParameters != null) {
                    validationParams = new ValidationParams(dHValidationParameters.getSeed(), dHValidationParameters.getCounter());
                }
                privateKeyInfo = new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.dhpublicnumber, new DomainParameters(dHParameters.getP(), dHParameters.getG(), dHParameters.getQ(), dHParameters.getJ(), validationParams).toASN1Primitive()), new ASN1Integer(this.getX()));
            } else {
                privateKeyInfo = new PrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.dhKeyAgreement, new DHParameter(this.dhSpec.getP(), this.dhSpec.getG(), this.dhSpec.getL()).toASN1Primitive()), new ASN1Integer(this.getX()));
            }
            return privateKeyInfo.getEncoded("DER");
        } catch (Exception exception) {
            return null;
        }
    }

    public String toString() {
        return DHUtil.privateKeyToString("DH", this.x, new DHParameters(this.dhSpec.getP(), this.dhSpec.getG()));
    }

    public DHParameterSpec getParams() {
        return this.dhSpec;
    }

    public BigInteger getX() {
        return this.x;
    }

    DHPrivateKeyParameters engineGetKeyParameters() {
        if (this.dhPrivateKey != null) {
            return this.dhPrivateKey;
        }
        if (this.dhSpec instanceof DHDomainParameterSpec) {
            return new DHPrivateKeyParameters(this.x, ((DHDomainParameterSpec)this.dhSpec).getDomainParameters());
        }
        return new DHPrivateKeyParameters(this.x, new DHParameters(this.dhSpec.getP(), this.dhSpec.getG(), null, this.dhSpec.getL()));
    }

    public boolean equals(Object object) {
        if (!(object instanceof DHPrivateKey)) {
            return false;
        }
        DHPrivateKey dHPrivateKey = (DHPrivateKey)object;
        return this.getX().equals(dHPrivateKey.getX()) && this.getParams().getG().equals(dHPrivateKey.getParams().getG()) && this.getParams().getP().equals(dHPrivateKey.getParams().getP()) && this.getParams().getL() == dHPrivateKey.getParams().getL();
    }

    public int hashCode() {
        return this.getX().hashCode() ^ this.getParams().getG().hashCode() ^ this.getParams().getP().hashCode() ^ this.getParams().getL();
    }

    public void setBagAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.attrCarrier.setBagAttribute(aSN1ObjectIdentifier, aSN1Encodable);
    }

    public ASN1Encodable getBagAttribute(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return this.attrCarrier.getBagAttribute(aSN1ObjectIdentifier);
    }

    public Enumeration getBagAttributeKeys() {
        return this.attrCarrier.getBagAttributeKeys();
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.dhSpec = new DHParameterSpec((BigInteger)objectInputStream.readObject(), (BigInteger)objectInputStream.readObject(), objectInputStream.readInt());
        this.info = null;
        this.attrCarrier = new PKCS12BagAttributeCarrierImpl();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.dhSpec.getP());
        objectOutputStream.writeObject(this.dhSpec.getG());
        objectOutputStream.writeInt(this.dhSpec.getL());
    }
}

