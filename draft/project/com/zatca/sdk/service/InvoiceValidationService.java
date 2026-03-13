/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.zatca.sdk.service.GeneratorTemplate;
import com.zatca.sdk.service.flow.ValidationProcessor;
import com.zatca.sdk.service.flow.ValidationProcessorImpl;
import com.zatca.sdk.service.validation.Result;
import java.io.File;
import org.apache.log4j.Logger;

public class InvoiceValidationService
extends GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(InvoiceValidationService.class);
    private ValidationProcessor validationProcessor = new ValidationProcessorImpl();
    private File invoice;
    private Result validationResult;

    @Override
    protected boolean loadInvoiceFile() {
        LOG.debug("load invoice file");
        this.invoice = new File(this.property.getInvoiceFileName());
        return this.invoice.exists();
    }

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
            LOG.debug("validate invoice");
            this.validationResult = this.validationProcessor.run(this.invoice);
            if (this.validationResult == null) {
                LOG.error("failed to validate invoice");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("failed to validate invoice - " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean generateOutput() {
        if (this.validationResult.isValid()) {
            LOG.info(" *** GLOBAL VALIDATION RESULT = PASSED ");
        } else {
            LOG.info(" *** GLOBAL VALIDATION RESULT = FAILED ");
        }
        return true;
    }
}

