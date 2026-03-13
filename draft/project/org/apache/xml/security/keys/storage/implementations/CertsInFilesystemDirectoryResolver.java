/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys.storage.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.keys.content.x509.XMLX509SKI;
import org.apache.xml.security.keys.storage.StorageResolverException;
import org.apache.xml.security.keys.storage.StorageResolverSpi;
import org.apache.xml.security.utils.Base64;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CertsInFilesystemDirectoryResolver
extends StorageResolverSpi {
    private static Log log = LogFactory.getLog(CertsInFilesystemDirectoryResolver.class);
    private String merlinsCertificatesDir = null;
    private List<X509Certificate> certs = new ArrayList<X509Certificate>();

    public CertsInFilesystemDirectoryResolver(String directoryName) throws StorageResolverException {
        this.merlinsCertificatesDir = directoryName;
        this.readCertsFromHarddrive();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void readCertsFromHarddrive() throws StorageResolverException {
        File certDir = new File(this.merlinsCertificatesDir);
        ArrayList<String> al = new ArrayList<String>();
        String[] names = certDir.list();
        for (int i = 0; i < names.length; ++i) {
            String currentFileName = names[i];
            if (!currentFileName.endsWith(".crt")) continue;
            al.add(names[i]);
        }
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ex) {
            throw new StorageResolverException("empty", ex);
        }
        if (cf == null) {
            throw new StorageResolverException("empty");
        }
        for (int i = 0; i < al.size(); ++i) {
            String filename = certDir.getAbsolutePath() + File.separator + (String)al.get(i);
            File file = new File(filename);
            boolean added = false;
            String dn = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                X509Certificate cert = (X509Certificate)cf.generateCertificate(fis);
                cert.checkValidity();
                this.certs.add(cert);
                dn = cert.getSubjectX500Principal().getName();
                added = true;
            } catch (FileNotFoundException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (CertificateNotYetValidException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (CertificateExpiredException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } catch (CertificateException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not add certificate from file " + filename, ex);
                }
            } finally {
                block34: {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException ex) {
                        if (!log.isDebugEnabled()) break block34;
                        log.debug("Could not add certificate from file " + filename, ex);
                    }
                }
            }
            if (!added || !log.isDebugEnabled()) continue;
            log.debug("Added certificate: " + dn);
        }
    }

    @Override
    public Iterator<Certificate> getIterator() {
        return new FilesystemIterator(this.certs);
    }

    public static void main(String[] unused) throws Exception {
        CertsInFilesystemDirectoryResolver krs = new CertsInFilesystemDirectoryResolver("data/ie/baltimore/merlin-examples/merlin-xmldsig-eighteen/certs");
        Iterator<Certificate> i = krs.getIterator();
        while (i.hasNext()) {
            X509Certificate cert = (X509Certificate)i.next();
            byte[] ski = XMLX509SKI.getSKIBytesFromCert(cert);
            System.out.println();
            System.out.println("Base64(SKI())=                 \"" + Base64.encode(ski) + "\"");
            System.out.println("cert.getSerialNumber()=        \"" + cert.getSerialNumber().toString() + "\"");
            System.out.println("cert.getSubjectX500Principal().getName()= \"" + cert.getSubjectX500Principal().getName() + "\"");
            System.out.println("cert.getIssuerX500Principal().getName()=  \"" + cert.getIssuerX500Principal().getName() + "\"");
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class FilesystemIterator
    implements Iterator<Certificate> {
        List<X509Certificate> certs = null;
        int i;

        public FilesystemIterator(List<X509Certificate> certs) {
            this.certs = certs;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            return this.i < this.certs.size();
        }

        @Override
        public Certificate next() {
            return this.certs.get(this.i++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove keys from KeyStore");
        }
    }
}

