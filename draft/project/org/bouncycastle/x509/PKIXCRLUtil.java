/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.x509;

import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.PKIXParameters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.StoreException;
import org.bouncycastle.x509.X509CRLStoreSelector;
import org.bouncycastle.x509.X509Store;

abstract class PKIXCRLUtil {
    PKIXCRLUtil() {
    }

    static Set findCRLs(X509CRLStoreSelector x509CRLStoreSelector, PKIXParameters pKIXParameters) throws AnnotatedException {
        HashSet hashSet = new HashSet();
        try {
            PKIXCRLUtil.findCRLs(hashSet, x509CRLStoreSelector, pKIXParameters.getCertStores());
        } catch (AnnotatedException annotatedException) {
            throw new AnnotatedException("Exception obtaining complete CRLs.", annotatedException);
        }
        return hashSet;
    }

    private static void findCRLs(HashSet hashSet, X509CRLStoreSelector x509CRLStoreSelector, List list) throws AnnotatedException {
        AnnotatedException annotatedException = null;
        boolean bl = false;
        for (Object e : list) {
            Object object;
            if (e instanceof X509Store) {
                object = (X509Store)e;
                try {
                    hashSet.addAll(((X509Store)object).getMatches((Selector)x509CRLStoreSelector));
                    bl = true;
                } catch (StoreException storeException) {
                    annotatedException = new AnnotatedException("Exception searching in X.509 CRL store.", storeException);
                }
                continue;
            }
            object = (CertStore)e;
            try {
                hashSet.addAll(((CertStore)object).getCRLs(x509CRLStoreSelector));
                bl = true;
            } catch (CertStoreException certStoreException) {
                annotatedException = new AnnotatedException("Exception searching in X.509 CRL store.", certStoreException);
            }
        }
        if (!bl && annotatedException != null) {
            throw annotatedException;
        }
    }
}

