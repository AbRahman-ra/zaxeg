/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.digitalsignature.service.model;

public class DigitalSignature {
    private String digitalSignature;
    private byte[] xmlHashing;

    public String getDigitalSignature() {
        return this.digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public byte[] getXmlHashing() {
        return this.xmlHashing;
    }

    public void setXmlHashing(byte[] xmlHashing) {
        this.xmlHashing = xmlHashing;
    }
}

