/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.storage.implementations;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.xml.security.keys.storage.StorageResolverException;
import org.apache.xml.security.keys.storage.StorageResolverSpi;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class KeyStoreResolver
extends StorageResolverSpi {
    private KeyStore keyStore = null;

    public KeyStoreResolver(KeyStore keyStore) throws StorageResolverException {
        this.keyStore = keyStore;
        try {
            keyStore.aliases();
        } catch (KeyStoreException ex) {
            throw new StorageResolverException("generic.EmptyMessage", ex);
        }
    }

    @Override
    public Iterator<Certificate> getIterator() {
        return new KeyStoreIterator(this.keyStore);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class KeyStoreIterator
    implements Iterator<Certificate> {
        KeyStore keyStore = null;
        Enumeration<String> aliases = null;
        Certificate nextCert = null;

        public KeyStoreIterator(KeyStore keyStore) {
            try {
                this.keyStore = keyStore;
                this.aliases = this.keyStore.aliases();
            } catch (KeyStoreException ex) {
                this.aliases = new Enumeration<String>(){

                    @Override
                    public boolean hasMoreElements() {
                        return false;
                    }

                    @Override
                    public String nextElement() {
                        return null;
                    }
                };
            }
        }

        @Override
        public boolean hasNext() {
            if (this.nextCert == null) {
                this.nextCert = this.findNextCert();
            }
            return this.nextCert != null;
        }

        @Override
        public Certificate next() {
            if (this.nextCert == null) {
                this.nextCert = this.findNextCert();
                if (this.nextCert == null) {
                    throw new NoSuchElementException();
                }
            }
            Certificate ret = this.nextCert;
            this.nextCert = null;
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }

        private Certificate findNextCert() {
            while (this.aliases.hasMoreElements()) {
                String alias = this.aliases.nextElement();
                try {
                    Certificate cert = this.keyStore.getCertificate(alias);
                    if (cert == null) continue;
                    return cert;
                } catch (KeyStoreException ex) {
                    return null;
                }
            }
            return null;
        }
    }
}

