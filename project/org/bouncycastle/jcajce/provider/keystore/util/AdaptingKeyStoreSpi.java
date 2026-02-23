/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.keystore.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import org.bouncycastle.jcajce.provider.keystore.pkcs12.PKCS12KeyStoreSpi;
import org.bouncycastle.jcajce.provider.keystore.util.JKSKeyStoreSpi;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.util.Properties;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class AdaptingKeyStoreSpi
extends KeyStoreSpi {
    public static final String COMPAT_OVERRIDE = "keystore.type.compat";
    private final JKSKeyStoreSpi jksStore;
    private final KeyStoreSpi primaryStore;
    private KeyStoreSpi keyStoreSpi;

    public AdaptingKeyStoreSpi(JcaJceHelper jcaJceHelper, KeyStoreSpi keyStoreSpi) {
        this.jksStore = new JKSKeyStoreSpi(jcaJceHelper);
        this.primaryStore = keyStoreSpi;
        this.keyStoreSpi = keyStoreSpi;
    }

    public boolean engineProbe(InputStream inputStream) throws IOException {
        if (this.keyStoreSpi instanceof PKCS12KeyStoreSpi) {
            return ((PKCS12KeyStoreSpi)this.keyStoreSpi).engineProbe(inputStream);
        }
        return false;
    }

    @Override
    public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return this.keyStoreSpi.engineGetKey(string, cArray);
    }

    @Override
    public Certificate[] engineGetCertificateChain(String string) {
        return this.keyStoreSpi.engineGetCertificateChain(string);
    }

    @Override
    public Certificate engineGetCertificate(String string) {
        return this.keyStoreSpi.engineGetCertificate(string);
    }

    @Override
    public Date engineGetCreationDate(String string) {
        return this.keyStoreSpi.engineGetCreationDate(string);
    }

    @Override
    public void engineSetKeyEntry(String string, Key key, char[] cArray, Certificate[] certificateArray) throws KeyStoreException {
        this.keyStoreSpi.engineSetKeyEntry(string, key, cArray, certificateArray);
    }

    @Override
    public void engineSetKeyEntry(String string, byte[] byArray, Certificate[] certificateArray) throws KeyStoreException {
        this.keyStoreSpi.engineSetKeyEntry(string, byArray, certificateArray);
    }

    @Override
    public void engineSetCertificateEntry(String string, Certificate certificate) throws KeyStoreException {
        this.keyStoreSpi.engineSetCertificateEntry(string, certificate);
    }

    @Override
    public void engineDeleteEntry(String string) throws KeyStoreException {
        this.keyStoreSpi.engineDeleteEntry(string);
    }

    @Override
    public Enumeration<String> engineAliases() {
        return this.keyStoreSpi.engineAliases();
    }

    @Override
    public boolean engineContainsAlias(String string) {
        return this.keyStoreSpi.engineContainsAlias(string);
    }

    @Override
    public int engineSize() {
        return this.keyStoreSpi.engineSize();
    }

    @Override
    public boolean engineIsKeyEntry(String string) {
        return this.keyStoreSpi.engineIsKeyEntry(string);
    }

    @Override
    public boolean engineIsCertificateEntry(String string) {
        return this.keyStoreSpi.engineIsCertificateEntry(string);
    }

    @Override
    public String engineGetCertificateAlias(Certificate certificate) {
        return this.keyStoreSpi.engineGetCertificateAlias(certificate);
    }

    @Override
    public void engineStore(OutputStream outputStream, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineStore(outputStream, cArray);
    }

    @Override
    public void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineStore(loadStoreParameter);
    }

    @Override
    public void engineLoad(InputStream inputStream, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (inputStream == null) {
            this.keyStoreSpi = this.primaryStore;
            this.keyStoreSpi.engineLoad(null, cArray);
        } else {
            if (Properties.isOverrideSet(COMPAT_OVERRIDE) || !(this.primaryStore instanceof PKCS12KeyStoreSpi)) {
                if (!inputStream.markSupported()) {
                    inputStream = new BufferedInputStream(inputStream);
                }
                inputStream.mark(8);
                this.keyStoreSpi = this.jksStore.engineProbe(inputStream) ? this.jksStore : this.primaryStore;
                inputStream.reset();
            } else {
                this.keyStoreSpi = this.primaryStore;
            }
            this.keyStoreSpi.engineLoad(inputStream, cArray);
        }
    }

    @Override
    public void engineLoad(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        this.keyStoreSpi.engineLoad(loadStoreParameter);
    }
}

