/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.validation.service.model;

public class ValidationPIHItem {
    private int index;
    private String xmHashing;
    private String xmlPIH;
    private boolean validPIH;

    public ValidationPIHItem(int index, String xmlDocHashing, String pihXmlDoc) {
        this.index = index;
        this.xmHashing = xmlDocHashing;
        this.xmlPIH = pihXmlDoc;
    }

    public String getXmHashing() {
        return this.xmHashing;
    }

    public void setXmHashing(String xmHashing) {
        this.xmHashing = xmHashing;
    }

    public String getXmlPIH() {
        return this.xmlPIH;
    }

    public void setXmlPIH(String xmlPIH) {
        this.xmlPIH = xmlPIH;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isValidPIH() {
        return this.validPIH;
    }

    public void setValidPIH(boolean validPIH) {
        this.validPIH = validPIH;
    }
}

