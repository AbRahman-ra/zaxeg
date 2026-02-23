/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public abstract class TransformService
implements Transform {
    private String algorithm;
    private String mechanism;
    private Provider provider;

    protected TransformService() {
    }

    public static TransformService getInstance(String algorithm, String mechanismType) throws NoSuchAlgorithmException {
        if (mechanismType == null || algorithm == null) {
            throw new NullPointerException();
        }
        return TransformService.findInstance(algorithm, mechanismType, null);
    }

    public static TransformService getInstance(String algorithm, String mechanismType, Provider provider) throws NoSuchAlgorithmException {
        if (mechanismType == null || algorithm == null || provider == null) {
            throw new NullPointerException();
        }
        return TransformService.findInstance(algorithm, mechanismType, provider);
    }

    public static TransformService getInstance(String algorithm, String mechanismType, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (mechanismType == null || algorithm == null || provider == null) {
            throw new NullPointerException();
        }
        Provider prov = Security.getProvider(provider);
        if (prov == null) {
            throw new NoSuchProviderException("cannot find provider named " + provider);
        }
        return TransformService.findInstance(algorithm, mechanismType, prov);
    }

    private static TransformService findInstance(String algorithm, String mechanismType, Provider provider) throws NoSuchAlgorithmException {
        Provider.Service ps;
        if (provider == null) {
            provider = TransformService.getProvider("TransformService", algorithm, mechanismType);
        }
        if ((ps = provider.getService("TransformService", algorithm)) == null) {
            throw new NoSuchAlgorithmException("no such algorithm: " + algorithm + " for provider " + provider.getName());
        }
        TransformService ts = (TransformService)ps.newInstance(null);
        ts.algorithm = algorithm;
        ts.mechanism = mechanismType;
        ts.provider = provider;
        return ts;
    }

    private static Provider getProvider(String engine, String alg, String mech) throws NoSuchAlgorithmException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(engine + "." + alg, "");
        map.put(engine + "." + alg + " " + "MechanismType", mech);
        Provider[] providers = Security.getProviders(map);
        if (providers == null) {
            if (mech.equals("DOM")) {
                map.clear();
                map.put(engine + "." + alg, "");
                providers = Security.getProviders(map);
                if (providers != null) {
                    return providers[0];
                }
            }
            throw new NoSuchAlgorithmException("Algorithm type " + alg + " not available");
        }
        return providers[0];
    }

    public final String getMechanismType() {
        return this.mechanism;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public abstract void init(TransformParameterSpec var1) throws InvalidAlgorithmParameterException;

    public abstract void marshalParams(XMLStructure var1, XMLCryptoContext var2) throws MarshalException;

    public abstract void init(XMLStructure var1, XMLCryptoContext var2) throws InvalidAlgorithmParameterException;
}

