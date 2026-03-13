/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce;

import java.io.OutputStream;
import java.security.KeyStore;
import org.bouncycastle.crypto.util.PBKDFConfig;

public class BCFKSStoreParameter
implements KeyStore.LoadStoreParameter {
    private final KeyStore.ProtectionParameter protectionParameter;
    private final PBKDFConfig storeConfig;
    private OutputStream out;

    public BCFKSStoreParameter(OutputStream outputStream, PBKDFConfig pBKDFConfig, char[] cArray) {
        this(outputStream, pBKDFConfig, new KeyStore.PasswordProtection(cArray));
    }

    public BCFKSStoreParameter(OutputStream outputStream, PBKDFConfig pBKDFConfig, KeyStore.ProtectionParameter protectionParameter) {
        this.out = outputStream;
        this.storeConfig = pBKDFConfig;
        this.protectionParameter = protectionParameter;
    }

    public KeyStore.ProtectionParameter getProtectionParameter() {
        return this.protectionParameter;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public PBKDFConfig getStorePBKDFConfig() {
        return this.storeConfig;
    }
}

