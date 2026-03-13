/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.keyinfo;

import java.math.BigInteger;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NoSuchMechanismException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;

public abstract class KeyInfoFactory {
    private String mechanismType;
    private Provider provider;

    protected KeyInfoFactory() {
    }

    public static KeyInfoFactory getInstance(String mechanismType) {
        if (mechanismType == null) {
            throw new NullPointerException("mechanismType cannot be null");
        }
        return KeyInfoFactory.findInstance(mechanismType, null);
    }

    private static KeyInfoFactory findInstance(String mechanismType, Provider provider) {
        Provider.Service ps;
        if (provider == null) {
            provider = KeyInfoFactory.getProvider("KeyInfoFactory", mechanismType);
        }
        if ((ps = provider.getService("KeyInfoFactory", mechanismType)) == null) {
            throw new NoSuchMechanismException("Cannot find " + mechanismType + " mechanism type");
        }
        try {
            KeyInfoFactory fac = (KeyInfoFactory)ps.newInstance(null);
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

    public static KeyInfoFactory getInstance(String mechanismType, Provider provider) {
        if (mechanismType == null) {
            throw new NullPointerException("mechanismType cannot be null");
        }
        if (provider == null) {
            throw new NullPointerException("provider cannot be null");
        }
        return KeyInfoFactory.findInstance(mechanismType, provider);
    }

    public static KeyInfoFactory getInstance(String mechanismType, String provider) throws NoSuchProviderException {
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
        return KeyInfoFactory.findInstance(mechanismType, prov);
    }

    public static KeyInfoFactory getInstance() {
        return KeyInfoFactory.getInstance("DOM");
    }

    public final String getMechanismType() {
        return this.mechanismType;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public abstract KeyInfo newKeyInfo(List var1);

    public abstract KeyInfo newKeyInfo(List var1, String var2);

    public abstract KeyName newKeyName(String var1);

    public abstract KeyValue newKeyValue(PublicKey var1) throws KeyException;

    public abstract PGPData newPGPData(byte[] var1);

    public abstract PGPData newPGPData(byte[] var1, byte[] var2, List var3);

    public abstract PGPData newPGPData(byte[] var1, List var2);

    public abstract RetrievalMethod newRetrievalMethod(String var1);

    public abstract RetrievalMethod newRetrievalMethod(String var1, String var2, List var3);

    public abstract X509Data newX509Data(List var1);

    public abstract X509IssuerSerial newX509IssuerSerial(String var1, BigInteger var2);

    public abstract boolean isFeatureSupported(String var1);

    public abstract URIDereferencer getURIDereferencer();

    public abstract KeyInfo unmarshalKeyInfo(XMLStructure var1) throws MarshalException;
}

