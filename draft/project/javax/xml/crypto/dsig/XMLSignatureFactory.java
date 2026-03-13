/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NoSuchMechanismException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public abstract class XMLSignatureFactory {
    private String mechanismType;
    private Provider provider;

    protected XMLSignatureFactory() {
    }

    public static XMLSignatureFactory getInstance(String mechanismType) {
        if (mechanismType == null) {
            throw new NullPointerException("mechanismType cannot be null");
        }
        return XMLSignatureFactory.findInstance(mechanismType, null);
    }

    private static XMLSignatureFactory findInstance(String mechanismType, Provider provider) {
        Provider.Service ps;
        if (provider == null) {
            provider = XMLSignatureFactory.getProvider("XMLSignatureFactory", mechanismType);
        }
        if ((ps = provider.getService("XMLSignatureFactory", mechanismType)) == null) {
            throw new NoSuchMechanismException("Cannot find " + mechanismType + " mechanism type");
        }
        try {
            XMLSignatureFactory fac = (XMLSignatureFactory)ps.newInstance(null);
            fac.mechanismType = mechanismType;
            fac.provider = provider;
            return fac;
        } catch (NoSuchAlgorithmException nsae) {
            throw new NoSuchMechanismException("Cannot find " + mechanismType + " mechanism type", nsae);
        }
    }

    private static Provider getProvider(String engine, String mech) {
        Provider[] providers = Security.getProviders(engine + "." + mech);
        if (providers == null) {
            throw new NoSuchMechanismException("Mechanism type " + mech + " not available");
        }
        return providers[0];
    }

    public static XMLSignatureFactory getInstance(String mechanismType, Provider provider) {
        if (mechanismType == null) {
            throw new NullPointerException("mechanismType cannot be null");
        }
        if (provider == null) {
            throw new NullPointerException("provider cannot be null");
        }
        return XMLSignatureFactory.findInstance(mechanismType, provider);
    }

    public static XMLSignatureFactory getInstance(String mechanismType, String provider) throws NoSuchProviderException {
        if (mechanismType == null) {
            throw new NullPointerException("mechanismType cannot be null");
        }
        if (provider == null) {
            throw new NullPointerException("provider cannot be null");
        }
        Provider prov = Security.getProvider(provider);
        if (prov == null) {
            throw new NoSuchProviderException("cannot find provider named " + provider);
        }
        return XMLSignatureFactory.findInstance(mechanismType, prov);
    }

    public static XMLSignatureFactory getInstance() {
        return XMLSignatureFactory.getInstance("DOM");
    }

    public final String getMechanismType() {
        return this.mechanismType;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public abstract XMLSignature newXMLSignature(SignedInfo var1, KeyInfo var2);

    public abstract XMLSignature newXMLSignature(SignedInfo var1, KeyInfo var2, List var3, String var4, String var5);

    public abstract Reference newReference(String var1, DigestMethod var2);

    public abstract Reference newReference(String var1, DigestMethod var2, List var3, String var4, String var5);

    public abstract Reference newReference(String var1, DigestMethod var2, List var3, String var4, String var5, byte[] var6);

    public abstract Reference newReference(String var1, DigestMethod var2, List var3, Data var4, List var5, String var6, String var7);

    public abstract SignedInfo newSignedInfo(CanonicalizationMethod var1, SignatureMethod var2, List var3);

    public abstract SignedInfo newSignedInfo(CanonicalizationMethod var1, SignatureMethod var2, List var3, String var4);

    public abstract XMLObject newXMLObject(List var1, String var2, String var3, String var4);

    public abstract Manifest newManifest(List var1);

    public abstract Manifest newManifest(List var1, String var2);

    public abstract SignatureProperty newSignatureProperty(List var1, String var2, String var3);

    public abstract SignatureProperties newSignatureProperties(List var1, String var2);

    public abstract DigestMethod newDigestMethod(String var1, DigestMethodParameterSpec var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract SignatureMethod newSignatureMethod(String var1, SignatureMethodParameterSpec var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract Transform newTransform(String var1, TransformParameterSpec var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract Transform newTransform(String var1, XMLStructure var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract CanonicalizationMethod newCanonicalizationMethod(String var1, C14NMethodParameterSpec var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract CanonicalizationMethod newCanonicalizationMethod(String var1, XMLStructure var2) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public final KeyInfoFactory getKeyInfoFactory() {
        return KeyInfoFactory.getInstance(this.getMechanismType(), this.getProvider());
    }

    public abstract XMLSignature unmarshalXMLSignature(XMLValidateContext var1) throws MarshalException;

    public abstract XMLSignature unmarshalXMLSignature(XMLStructure var1) throws MarshalException;

    public abstract boolean isFeatureSupported(String var1);

    public abstract URIDereferencer getURIDereferencer();
}

