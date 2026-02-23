/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.validation.service.model;

public enum InvoiceType {
    STANDARD("01"),
    SIMPLIFIED("02");

    String code;

    private InvoiceType(String code) {
        this.code = code;
    }

    String getCode() {
        return this.code;
    }

    public static InvoiceType byCode(String code) {
        for (InvoiceType invoiceType : InvoiceType.values()) {
            if (!invoiceType.getCode().equalsIgnoreCase(code)) continue;
            return invoiceType;
        }
        return null;
    }
}

