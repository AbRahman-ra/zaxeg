/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.validation.service.model;

import com.gazt.einvoicing.hashing.validation.service.model.InvoiceType;

public class HashValidationResult {
    private boolean valid;
    private String xmlInvoiceHash;
    private InvoiceType invoiceType;

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getXmlInvoiceHash() {
        return this.xmlInvoiceHash;
    }

    public void setXmlInvoiceHash(String xmlInvoiceHash) {
        this.xmlInvoiceHash = xmlInvoiceHash;
    }

    public InvoiceType getInvoiceType() {
        return this.invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }
}

