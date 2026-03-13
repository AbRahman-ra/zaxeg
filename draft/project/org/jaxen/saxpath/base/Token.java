/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath.base;

class Token {
    private int tokenType;
    private String parseText;
    private int tokenBegin;
    private int tokenEnd;

    Token(int tokenType, String parseText, int tokenBegin, int tokenEnd) {
        this.setTokenType(tokenType);
        this.setParseText(parseText);
        this.setTokenBegin(tokenBegin);
        this.setTokenEnd(tokenEnd);
    }

    private void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    int getTokenType() {
        return this.tokenType;
    }

    private void setParseText(String parseText) {
        this.parseText = parseText;
    }

    String getTokenText() {
        return this.parseText.substring(this.getTokenBegin(), this.getTokenEnd());
    }

    private void setTokenBegin(int tokenBegin) {
        this.tokenBegin = tokenBegin;
    }

    int getTokenBegin() {
        return this.tokenBegin;
    }

    private void setTokenEnd(int tokenEnd) {
        this.tokenEnd = tokenEnd;
    }

    int getTokenEnd() {
        return this.tokenEnd;
    }

    public String toString() {
        return "[ (" + this.tokenType + ") (" + this.getTokenText() + ")";
    }
}

