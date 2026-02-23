/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import org.bouncycastle.jcajce.PKIXCRLStore;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Iterable;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

class CrlCache {
    private static final int DEFAULT_TIMEOUT = 15000;
    private static Map<URI, WeakReference<PKIXCRLStore>> cache = Collections.synchronizedMap(new WeakHashMap());

    CrlCache() {
    }

    static synchronized PKIXCRLStore getCrl(CertificateFactory certificateFactory, Date date, URI uRI) throws IOException, CRLException {
        PKIXCRLStore pKIXCRLStore = null;
        WeakReference<PKIXCRLStore> weakReference = cache.get(uRI);
        if (weakReference != null) {
            pKIXCRLStore = (PKIXCRLStore)weakReference.get();
        }
        if (pKIXCRLStore != null) {
            boolean bl = false;
            for (X509CRL x509CRL : pKIXCRLStore.getMatches(null)) {
                Date date2 = x509CRL.getNextUpdate();
                if (date2 == null || !date2.before(date)) continue;
                bl = true;
                break;
            }
            if (!bl) {
                return pKIXCRLStore;
            }
        }
        Collection collection = uRI.getScheme().equals("ldap") ? CrlCache.getCrlsFromLDAP(certificateFactory, uRI) : CrlCache.getCrls(certificateFactory, uRI);
        LocalCRLStore localCRLStore = new LocalCRLStore(new CollectionStore<CRL>(collection));
        cache.put(uRI, new WeakReference<Object>(localCRLStore));
        return localCRLStore;
    }

    private static Collection getCrlsFromLDAP(CertificateFactory certificateFactory, URI uRI) throws IOException, CRLException {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        hashtable.put("java.naming.provider.url", uRI.toString());
        byte[] byArray = null;
        try {
            InitialDirContext initialDirContext = new InitialDirContext(hashtable);
            Attributes attributes = initialDirContext.getAttributes("");
            Attribute attribute = attributes.get("certificateRevocationList;binary");
            byArray = (byte[])attribute.get();
        } catch (NamingException namingException) {
            throw new CRLException("issue connecting to: " + uRI.toString(), namingException);
        }
        if (byArray == null || byArray.length == 0) {
            throw new CRLException("no CRL returned from: " + uRI);
        }
        return certificateFactory.generateCRLs(new ByteArrayInputStream(byArray));
    }

    private static Collection getCrls(CertificateFactory certificateFactory, URI uRI) throws IOException, CRLException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRI.toURL().openConnection();
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(15000);
        InputStream inputStream = httpURLConnection.getInputStream();
        Collection<? extends CRL> collection = certificateFactory.generateCRLs(inputStream);
        inputStream.close();
        return collection;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class LocalCRLStore<T extends CRL>
    implements PKIXCRLStore,
    Iterable<CRL> {
        private Collection<CRL> _local;

        public LocalCRLStore(Store<CRL> store) {
            this._local = new ArrayList<CRL>(store.getMatches(null));
        }

        @Override
        public Collection getMatches(Selector selector) {
            if (selector == null) {
                return new ArrayList<CRL>(this._local);
            }
            ArrayList<CRL> arrayList = new ArrayList<CRL>();
            for (CRL cRL : this._local) {
                if (!selector.match(cRL)) continue;
                arrayList.add(cRL);
            }
            return arrayList;
        }

        @Override
        public Iterator<CRL> iterator() {
            return this.getMatches((Selector)null).iterator();
        }
    }
}

