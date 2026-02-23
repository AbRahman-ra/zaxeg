/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.keystore.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.jcajce.BCLoadStoreParameter;
import org.bouncycastle.jcajce.provider.keystore.util.ParameterUtil;
import org.bouncycastle.jcajce.provider.util.DigestFactory;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.io.Streams;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class JKSKeyStoreSpi
extends KeyStoreSpi {
    private static final String NOT_IMPLEMENTED_MESSAGE = "BC JKS store is read-only and only supports certificate entries";
    private final Hashtable<String, BCJKSTrustedCertEntry> certificateEntries = new Hashtable();
    private final JcaJceHelper helper;

    public JKSKeyStoreSpi(JcaJceHelper jcaJceHelper) {
        this.helper = jcaJceHelper;
    }

    public boolean engineProbe(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = inputStream instanceof DataInputStream ? (DataInputStream)inputStream : new DataInputStream(inputStream);
        int n = dataInputStream.readInt();
        int n2 = dataInputStream.readInt();
        return n == -17957139 && (n2 == 1 || n2 == 2);
    }

    @Override
    public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return null;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String string) {
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Certificate engineGetCertificate(String string) {
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            BCJKSTrustedCertEntry bCJKSTrustedCertEntry = this.certificateEntries.get(string);
            if (bCJKSTrustedCertEntry != null) {
                return bCJKSTrustedCertEntry.cert;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Date engineGetCreationDate(String string) {
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            BCJKSTrustedCertEntry bCJKSTrustedCertEntry = this.certificateEntries.get(string);
            if (bCJKSTrustedCertEntry != null) {
                return bCJKSTrustedCertEntry.date;
            }
        }
        return null;
    }

    @Override
    public void engineSetKeyEntry(String string, Key key, char[] cArray, Certificate[] certificateArray) throws KeyStoreException {
        throw new KeyStoreException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public void engineSetKeyEntry(String string, byte[] byArray, Certificate[] certificateArray) throws KeyStoreException {
        throw new KeyStoreException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public void engineSetCertificateEntry(String string, Certificate certificate) throws KeyStoreException {
        throw new KeyStoreException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public void engineDeleteEntry(String string) throws KeyStoreException {
        throw new KeyStoreException(NOT_IMPLEMENTED_MESSAGE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Enumeration<String> engineAliases() {
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            return this.certificateEntries.keys();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean engineContainsAlias(String string) {
        if (string == null) {
            throw new NullPointerException("alias value is null");
        }
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            return this.certificateEntries.containsKey(string);
        }
    }

    @Override
    public int engineSize() {
        return this.certificateEntries.size();
    }

    @Override
    public boolean engineIsKeyEntry(String string) {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean engineIsCertificateEntry(String string) {
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            return this.certificateEntries.containsKey(string);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String engineGetCertificateAlias(Certificate certificate) {
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            for (Map.Entry<String, BCJKSTrustedCertEntry> entry : this.certificateEntries.entrySet()) {
                if (!entry.getValue().cert.equals(certificate)) continue;
                return entry.getKey();
            }
            return null;
        }
    }

    @Override
    public void engineStore(OutputStream outputStream, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new IOException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (loadStoreParameter == null) {
            this.engineLoad(null, null);
        } else if (loadStoreParameter instanceof BCLoadStoreParameter) {
            BCLoadStoreParameter bCLoadStoreParameter = (BCLoadStoreParameter)loadStoreParameter;
            this.engineLoad(bCLoadStoreParameter.getInputStream(), ParameterUtil.extractPassword(loadStoreParameter));
        } else {
            throw new IllegalArgumentException("no support for 'param' of type " + loadStoreParameter.getClass().getName());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void engineLoad(InputStream inputStream, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (inputStream == null) {
            return;
        }
        ErasableByteStream erasableByteStream = this.validateStream(inputStream, cArray);
        Hashtable<String, BCJKSTrustedCertEntry> hashtable = this.certificateEntries;
        synchronized (hashtable) {
            try {
                block25: {
                    DataInputStream dataInputStream = new DataInputStream(erasableByteStream);
                    int n = dataInputStream.readInt();
                    int n2 = dataInputStream.readInt();
                    if (n != -17957139) break block25;
                    CertificateFactory certificateFactory = null;
                    Hashtable<String, CertificateFactory> hashtable2 = null;
                    switch (n2) {
                        case 1: {
                            certificateFactory = this.createCertFactory("X.509");
                            break;
                        }
                        case 2: {
                            hashtable2 = new Hashtable<String, CertificateFactory>();
                            break;
                        }
                        default: {
                            throw new IllegalStateException("unable to discern store version");
                        }
                    }
                    int n3 = dataInputStream.readInt();
                    block17: for (int i = 0; i < n3; ++i) {
                        int n4 = dataInputStream.readInt();
                        switch (n4) {
                            case 1: {
                                throw new IOException(NOT_IMPLEMENTED_MESSAGE);
                            }
                            case 2: {
                                Certificate certificate;
                                String string = dataInputStream.readUTF();
                                Date date = new Date(dataInputStream.readLong());
                                if (n2 == 2) {
                                    String string2 = dataInputStream.readUTF();
                                    if (hashtable2.containsKey(string2)) {
                                        certificateFactory = (CertificateFactory)hashtable2.get(string2);
                                    } else {
                                        certificateFactory = this.createCertFactory(string2);
                                        hashtable2.put(string2, certificateFactory);
                                    }
                                }
                                int n5 = dataInputStream.readInt();
                                byte[] byArray = new byte[n5];
                                dataInputStream.readFully(byArray);
                                ErasableByteStream erasableByteStream2 = new ErasableByteStream(byArray, 0, byArray.length);
                                try {
                                    certificate = certificateFactory.generateCertificate(erasableByteStream2);
                                    if (erasableByteStream2.available() != 0) {
                                        throw new IOException("password incorrect or store tampered with");
                                    }
                                } finally {
                                    erasableByteStream2.erase();
                                }
                                this.certificateEntries.put(string, new BCJKSTrustedCertEntry(date, certificate));
                                continue block17;
                            }
                            default: {
                                throw new IllegalStateException("unable to discern entry type");
                            }
                        }
                    }
                }
                if (erasableByteStream.available() != 0) {
                    throw new IOException("password incorrect or store tampered with");
                }
            } finally {
                erasableByteStream.erase();
            }
        }
    }

    private CertificateFactory createCertFactory(String string) throws CertificateException {
        if (this.helper != null) {
            try {
                return this.helper.createCertificateFactory(string);
            } catch (NoSuchProviderException noSuchProviderException) {
                throw new CertificateException(noSuchProviderException.toString());
            }
        }
        return CertificateFactory.getInstance(string);
    }

    private void addPassword(Digest digest, char[] cArray) throws IOException {
        for (int i = 0; i < cArray.length; ++i) {
            digest.update((byte)(cArray[i] >> 8));
            digest.update((byte)cArray[i]);
        }
        digest.update(Strings.toByteArray("Mighty Aphrodite"), 0, 16);
    }

    private ErasableByteStream validateStream(InputStream inputStream, char[] cArray) throws IOException {
        Digest digest = DigestFactory.getDigest("SHA-1");
        byte[] byArray = Streams.readAll(inputStream);
        if (cArray != null) {
            this.addPassword(digest, cArray);
            digest.update(byArray, 0, byArray.length - digest.getDigestSize());
            byte[] byArray2 = new byte[digest.getDigestSize()];
            digest.doFinal(byArray2, 0);
            byte[] byArray3 = new byte[byArray2.length];
            System.arraycopy(byArray, byArray.length - byArray2.length, byArray3, 0, byArray2.length);
            if (!Arrays.constantTimeAreEqual(byArray2, byArray3)) {
                Arrays.fill(byArray, (byte)0);
                throw new IOException("password incorrect or store tampered with");
            }
            return new ErasableByteStream(byArray, 0, byArray.length - byArray2.length);
        }
        return new ErasableByteStream(byArray, 0, byArray.length - digest.getDigestSize());
    }

    private static final class BCJKSTrustedCertEntry {
        final Date date;
        final Certificate cert;

        public BCJKSTrustedCertEntry(Date date, Certificate certificate) {
            this.date = date;
            this.cert = certificate;
        }
    }

    private static final class ErasableByteStream
    extends ByteArrayInputStream {
        public ErasableByteStream(byte[] byArray, int n, int n2) {
            super(byArray, n, n2);
        }

        public void erase() {
            Arrays.fill(this.buf, (byte)0);
        }
    }
}

