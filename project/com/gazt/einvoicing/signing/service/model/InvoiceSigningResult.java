/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.signing.service.model;

public class InvoiceSigningResult {
    private String singedXML;
    private boolean includesQRCodeAlready;
    private String invoiceHash;
    private String qrCode;

    public String getSingedXML() {
        return this.singedXML;
    }

    public void setSingedXML(String singedXML) {
        this.singedXML = singedXML;
    }

    public boolean isIncludesQRCodeAlready() {
        return this.includesQRCodeAlready;
    }

    public void setIncludesQRCodeAlready(boolean includesQRCodeAlready) {
        this.includesQRCodeAlready = includesQRCodeAlready;
    }

    public String getInvoiceHash() {
        return this.invoiceHash;
    }

    public void setInvoiceHash(String invoiceHash) {
        this.invoiceHash = invoiceHash;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}

