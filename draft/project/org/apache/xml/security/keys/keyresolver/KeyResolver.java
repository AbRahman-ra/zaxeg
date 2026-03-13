/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.keyresolver;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.crypto.SecretKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.apache.xml.security.keys.keyresolver.KeyResolverSpi;
import org.apache.xml.security.keys.keyresolver.implementations.DEREncodedKeyValueResolver;
import org.apache.xml.security.keys.keyresolver.implementations.DSAKeyValueResolver;
import org.apache.xml.security.keys.keyresolver.implementations.KeyInfoReferenceResolver;
import org.apache.xml.security.keys.keyresolver.implementations.RSAKeyValueResolver;
import org.apache.xml.security.keys.keyresolver.implementations.RetrievalMethodResolver;
import org.apache.xml.security.keys.keyresolver.implementations.X509CertificateResolver;
import org.apache.xml.security.keys.keyresolver.implementations.X509DigestResolver;
import org.apache.xml.security.keys.keyresolver.implementations.X509IssuerSerialResolver;
import org.apache.xml.security.keys.keyresolver.implementations.X509SKIResolver;
import org.apache.xml.security.keys.keyresolver.implementations.X509SubjectNameResolver;
import org.apache.xml.security.keys.storage.StorageResolver;
import org.apache.xml.security.utils.ClassLoaderUtils;
import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class KeyResolver {
    private static Log log = LogFactory.getLog(KeyResolver.class);
    private static List<KeyResolver> resolverVector = new CopyOnWriteArrayList<KeyResolver>();
    private final KeyResolverSpi resolverSpi;

    private KeyResolver(KeyResolverSpi keyResolverSpi) {
        this.resolverSpi = keyResolverSpi;
    }

    public static int length() {
        return resolverVector.size();
    }

    public static final X509Certificate getX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        for (KeyResolver resolver : resolverVector) {
            X509Certificate cert;
            if (resolver == null) {
                Object[] exArgs = new Object[]{element != null && element.getNodeType() == 1 ? element.getTagName() : "null"};
                throw new KeyResolverException("utils.resolver.noClass", exArgs);
            }
            if (log.isDebugEnabled()) {
                log.debug("check resolvability by class " + resolver.getClass());
            }
            if ((cert = resolver.resolveX509Certificate(element, baseURI, storage)) == null) continue;
            return cert;
        }
        Object[] exArgs = new Object[]{element != null && element.getNodeType() == 1 ? element.getTagName() : "null"};
        throw new KeyResolverException("utils.resolver.noClass", exArgs);
    }

    public static final PublicKey getPublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        for (KeyResolver resolver : resolverVector) {
            PublicKey cert;
            if (resolver == null) {
                Object[] exArgs = new Object[]{element != null && element.getNodeType() == 1 ? element.getTagName() : "null"};
                throw new KeyResolverException("utils.resolver.noClass", exArgs);
            }
            if (log.isDebugEnabled()) {
                log.debug("check resolvability by class " + resolver.getClass());
            }
            if ((cert = resolver.resolvePublicKey(element, baseURI, storage)) == null) continue;
            return cert;
        }
        Object[] exArgs = new Object[]{element != null && element.getNodeType() == 1 ? element.getTagName() : "null"};
        throw new KeyResolverException("utils.resolver.noClass", exArgs);
    }

    public static void register(String className, boolean globalResolver) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaUtils.checkRegisterPermission();
        KeyResolverSpi keyResolverSpi = (KeyResolverSpi)ClassLoaderUtils.loadClass(className, KeyResolver.class).newInstance();
        keyResolverSpi.setGlobalResolver(globalResolver);
        KeyResolver.register(keyResolverSpi, false);
    }

    public static void registerAtStart(String className, boolean globalResolver) {
        JavaUtils.checkRegisterPermission();
        KeyResolverSpi keyResolverSpi = null;
        ReflectiveOperationException ex = null;
        try {
            keyResolverSpi = (KeyResolverSpi)ClassLoaderUtils.loadClass(className, KeyResolver.class).newInstance();
        } catch (ClassNotFoundException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        } catch (InstantiationException e) {
            ex = e;
        }
        if (ex != null) {
            throw (IllegalArgumentException)new IllegalArgumentException("Invalid KeyResolver class name").initCause(ex);
        }
        keyResolverSpi.setGlobalResolver(globalResolver);
        KeyResolver.register(keyResolverSpi, true);
    }

    public static void register(KeyResolverSpi keyResolverSpi, boolean start) {
        JavaUtils.checkRegisterPermission();
        KeyResolver resolver = new KeyResolver(keyResolverSpi);
        if (start) {
            resolverVector.add(0, resolver);
        } else {
            resolverVector.add(resolver);
        }
    }

    public static void registerClassNames(List<String> classNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        JavaUtils.checkRegisterPermission();
        ArrayList<KeyResolver> keyResolverList = new ArrayList<KeyResolver>(classNames.size());
        for (String className : classNames) {
            KeyResolverSpi keyResolverSpi = (KeyResolverSpi)ClassLoaderUtils.loadClass(className, KeyResolver.class).newInstance();
            keyResolverSpi.setGlobalResolver(false);
            keyResolverList.add(new KeyResolver(keyResolverSpi));
        }
        resolverVector.addAll(keyResolverList);
    }

    public static void registerDefaultResolvers() {
        ArrayList<KeyResolver> keyResolverList = new ArrayList<KeyResolver>();
        keyResolverList.add(new KeyResolver(new RSAKeyValueResolver()));
        keyResolverList.add(new KeyResolver(new DSAKeyValueResolver()));
        keyResolverList.add(new KeyResolver(new X509CertificateResolver()));
        keyResolverList.add(new KeyResolver(new X509SKIResolver()));
        keyResolverList.add(new KeyResolver(new RetrievalMethodResolver()));
        keyResolverList.add(new KeyResolver(new X509SubjectNameResolver()));
        keyResolverList.add(new KeyResolver(new X509IssuerSerialResolver()));
        keyResolverList.add(new KeyResolver(new DEREncodedKeyValueResolver()));
        keyResolverList.add(new KeyResolver(new KeyInfoReferenceResolver()));
        keyResolverList.add(new KeyResolver(new X509DigestResolver()));
        resolverVector.addAll(keyResolverList);
    }

    public PublicKey resolvePublicKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return this.resolverSpi.engineLookupAndResolvePublicKey(element, baseURI, storage);
    }

    public X509Certificate resolveX509Certificate(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return this.resolverSpi.engineLookupResolveX509Certificate(element, baseURI, storage);
    }

    public SecretKey resolveSecretKey(Element element, String baseURI, StorageResolver storage) throws KeyResolverException {
        return this.resolverSpi.engineLookupAndResolveSecretKey(element, baseURI, storage);
    }

    public void setProperty(String key, String value) {
        this.resolverSpi.engineSetProperty(key, value);
    }

    public String getProperty(String key) {
        return this.resolverSpi.engineGetProperty(key);
    }

    public boolean understandsProperty(String propertyToTest) {
        return this.resolverSpi.understandsProperty(propertyToTest);
    }

    public String resolverClassName() {
        return this.resolverSpi.getClass().getName();
    }

    public static Iterator<KeyResolverSpi> iterator() {
        return new ResolverIterator(resolverVector);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class ResolverIterator
    implements Iterator<KeyResolverSpi> {
        List<KeyResolver> res;
        Iterator<KeyResolver> it;

        public ResolverIterator(List<KeyResolver> list) {
            this.res = list;
            this.it = this.res.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.it.hasNext();
        }

        @Override
        public KeyResolverSpi next() {
            KeyResolver resolver = this.it.next();
            if (resolver == null) {
                throw new RuntimeException("utils.resolver.noClass");
            }
            return resolver.resolverSpi;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove resolvers using the iterator");
        }
    }
}

