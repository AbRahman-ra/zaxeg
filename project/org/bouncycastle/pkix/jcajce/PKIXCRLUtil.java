/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.pkix.jcajce;

import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bouncycastle.jcajce.PKIXCRLStoreSelector;
import org.bouncycastle.pkix.jcajce.AnnotatedException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;

abstract class PKIXCRLUtil {
    PKIXCRLUtil() {
    }

    static Set findCRLs(PKIXCRLStoreSelector pKIXCRLStoreSelector, Date date, List list, List list2) throws AnnotatedException {
        HashSet hashSet = new HashSet();
        try {
            PKIXCRLUtil.findCRLs(hashSet, pKIXCRLStoreSelector, list2);
            PKIXCRLUtil.findCRLs(hashSet, pKIXCRLStoreSelector, list);
        } catch (AnnotatedException annotatedException) {
            throw new AnnotatedException("Exception obtaining complete CRLs.", annotatedException);
        }
        HashSet<X509CRL> hashSet2 = new HashSet<X509CRL>();
        for (X509CRL x509CRL : hashSet) {
            X509Certificate x509Certificate;
            Date date2 = x509CRL.getNextUpdate();
            if (date2 != null && !date2.after(date) || null != (x509Certificate = pKIXCRLStoreSelector.getCertificateChecking()) && !x509CRL.getThisUpdate().before(x509Certificate.getNotAfter())) continue;
            hashSet2.add(x509CRL);
        }
        return hashSet2;
    }

    private static void findCRLs(HashSet hashSet, PKIXCRLStoreSelector pKIXCRLStoreSelector, List list) throws AnnotatedException {
        AnnotatedException annotatedException = null;
        boolean bl = false;
        for (Object e : list) {
            Object object;
            if (e instanceof Store) {
                object = (Store)e;
                try {
                    hashSet.addAll(object.getMatches(pKIXCRLStoreSelector));
                    bl = true;
                } catch (StoreException storeException) {
                    annotatedException = new AnnotatedException("Exception searching in X.509 CRL store.", storeException);
                }
                continue;
            }
            object = (CertStore)e;
            try {
                hashSet.addAll(PKIXCRLStoreSelector.getCRLs(pKIXCRLStoreSelector, (CertStore)object));
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

