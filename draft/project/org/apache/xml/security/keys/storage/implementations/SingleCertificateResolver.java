/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.storage.implementations;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.xml.security.keys.storage.StorageResolverSpi;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SingleCertificateResolver
extends StorageResolverSpi {
    private X509Certificate certificate = null;

    public SingleCertificateResolver(X509Certificate x509cert) {
        this.certificate = x509cert;
    }

    @Override
    public Iterator<Certificate> getIterator() {
        return new InternalIterator(this.certificate);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class InternalIterator
    implements Iterator<Certificate> {
        boolean alreadyReturned = false;
        X509Certificate certificate = null;

        public InternalIterator(X509Certificate x509cert) {
            this.certificate = x509cert;
        }

        @Override
        public boolean hasNext() {
            return !this.alreadyReturned;
        }

        @Override
        public Certificate next() {
            if (this.alreadyReturned) {
                throw new NoSuchElementException();
            }
            this.alreadyReturned = true;
            return this.certificate;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }
    }
}

