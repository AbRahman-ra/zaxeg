/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.storage;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.keys.storage.StorageResolverException;
import org.apache.xml.security.keys.storage.StorageResolverSpi;
import org.apache.xml.security.keys.storage.implementations.KeyStoreResolver;
import org.apache.xml.security.keys.storage.implementations.SingleCertificateResolver;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class StorageResolver {
    private static Log log = LogFactory.getLog(StorageResolver.class);
    private List<StorageResolverSpi> storageResolvers = null;

    public StorageResolver() {
    }

    public StorageResolver(StorageResolverSpi resolver) {
        this.add(resolver);
    }

    public void add(StorageResolverSpi resolver) {
        if (this.storageResolvers == null) {
            this.storageResolvers = new ArrayList<StorageResolverSpi>();
        }
        this.storageResolvers.add(resolver);
    }

    public StorageResolver(KeyStore keyStore) {
        this.add(keyStore);
    }

    public void add(KeyStore keyStore) {
        try {
            this.add(new KeyStoreResolver(keyStore));
        } catch (StorageResolverException ex) {
            log.error("Could not add KeyStore because of: ", ex);
        }
    }

    public StorageResolver(X509Certificate x509certificate) {
        this.add(x509certificate);
    }

    public void add(X509Certificate x509certificate) {
        this.add(new SingleCertificateResolver(x509certificate));
    }

    public Iterator<Certificate> getIterator() {
        return new StorageResolverIterator(this.storageResolvers.iterator());
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class StorageResolverIterator
    implements Iterator<Certificate> {
        Iterator<StorageResolverSpi> resolvers = null;
        Iterator<Certificate> currentResolver = null;

        public StorageResolverIterator(Iterator<StorageResolverSpi> resolvers) {
            this.resolvers = resolvers;
            this.currentResolver = this.findNextResolver();
        }

        @Override
        public boolean hasNext() {
            if (this.currentResolver == null) {
                return false;
            }
            if (this.currentResolver.hasNext()) {
                return true;
            }
            this.currentResolver = this.findNextResolver();
            return this.currentResolver != null;
        }

        @Override
        public Certificate next() {
            if (this.hasNext()) {
                return this.currentResolver.next();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }

        private Iterator<Certificate> findNextResolver() {
            while (this.resolvers.hasNext()) {
                StorageResolverSpi resolverSpi = this.resolvers.next();
                Iterator<Certificate> iter = resolverSpi.getIterator();
                if (!iter.hasNext()) continue;
                return iter;
            }
            return null;
        }
    }
}

