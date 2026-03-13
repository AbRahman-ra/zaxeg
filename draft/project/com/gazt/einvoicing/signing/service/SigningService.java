/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.signing.service;

import com.gazt.einvoicing.signing.service.model.InvoiceSigningResult;
import java.io.InputStream;
import java.security.PrivateKey;

public interface SigningService {
    public InvoiceSigningResult signDocument(String var1, InputStream var2, InputStream var3, String var4) throws Exception;

    public InvoiceSigningResult signDocument(String var1, PrivateKey var2, String var3, String var4) throws Exception;

    public String generateInvoiceHash(String var1) throws Exception;
}

