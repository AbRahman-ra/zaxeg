/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.gazt.einvoicing.signing.service.SigningService;
import com.gazt.einvoicing.signing.service.impl.SigningServiceImpl;
import com.zatca.sdk.service.GeneratorTemplate;
import org.apache.log4j.Logger;

public class HashGenerationService
extends GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(HashGenerationService.class);
    private SigningService signingService = new SigningServiceImpl();
    private String invoiceHash;

    @Override
    public boolean loadInput() {
        return this.loadInvoiceFile();
    }

    @Override
    public boolean validateInput() {
        return true;
    }

    @Override
    public boolean process() {
        try {
            LOG.debug("generate invoice hash");
            this.invoiceHash = this.signingService.generateInvoiceHash(this.invoiceStr);
        } catch (Exception e) {
            LOG.error("failed to generate hash - " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean generateOutput() {
        LOG.info("invoice hash has been generated successfully");
        LOG.info(" *** INVOICE HASH = " + this.invoiceHash);
        return true;
    }
}

