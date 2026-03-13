/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.w3c.dom.Element;

public abstract class KeyResolverSpi {
    protected Map<String, String> properties = null;
    protected boolean globalResolver = false;
    protected boolean secureValidation;

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public boolean engineCanResolve(Element element, String baseURI, StorageResolver storage) {
        throw new UnsupportedOperationException();
    }

    public PublicKey engineResolvePublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        throw new UnsupportedOperationException();
    }

    public PublicKey engineLookupAndResolvePublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        KeyResolverSpi tmp = this.cloneIfNeeded();
        if (!tmp.engineCanResolve(element, baseURI, storage)) {
            return null;
        }
        return tmp.engineResolvePublicKey(element, baseURI, storage);
    }

    private KeyResolverSpi cloneIfNeeded() throws KeyResolverException {
        KeyResolverSpi tmp = this;
        if (this.globalResolver) {
            try {
                tmp = (KeyResolverSpi)this.getClass().newInstance();
            } catch (InstantiationException e) {
                throw new KeyResolverException("", e);
            } catch (IllegalAccessException e) {
                throw new KeyResolverException("", e);
            }
        }
        return tmp;
    }

    public X509Certificate engineResolveX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        throw new UnsupportedOperationException();
    }

    public X509Certificate engineLookupResolveX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        KeyResolverSpi tmp = this.cloneIfNeeded();
        if (!tmp.engineCanResolve(element, baseURI, storage)) {
            return null;
        }
        return tmp.engineResolveX509Certificate(element, baseURI, storage);
    }

    public SecretKey engineResolveSecretKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        throw new UnsupportedOperationException();
    }

    public SecretKey engineLookupAndResolveSecretKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        KeyResolverSpi tmp = this.cloneIfNeeded();
        if (!tmp.engineCanResolve(element, baseURI, storage)) {
            return null;
        }
        return tmp.engineResolveSecretKey(element, baseURI, storage);
    }

    public PrivateKey engineLookupAndResolvePrivateKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return null;
    }

    public void engineSetProperty(String key, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<String, String>();
        }
        this.properties.put(key, value);
    }

    public String engineGetProperty(String key) {
        if (this.properties == null) {
            return null;
        }
        return this.properties.get(key);
    }

    public boolean understandsProperty(String propertyToTest) {
        if (this.properties == null) {
            return false;
        }
        return this.properties.get(propertyToTest) != null;
    }

    public void setGlobalResolver(boolean globalResolver) {
        this.globalResolver = globalResolver;
    }
}

