/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zatca.sdk.service.validation.StageEnum;
import java.util.Map;

public class Result {
    private boolean valid = true;
    private Map<String, String> error;
    private boolean validQrCode = false;
    private boolean validSignature = false;
    @JsonIgnore
    private StageEnum stage;

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Map<String, String> getError() {
        return this.error;
    }

    public void setError(Map<String, String> error) {
        this.error = error;
    }

    public boolean isValidQrCode() {
        return this.validQrCode;
    }

    public void setValidQrCode(boolean validQrCode) {
        this.validQrCode = validQrCode;
    }

    public boolean isValidSignature() {
        return this.validSignature;
    }

    public void setValidSignature(boolean validSignature) {
        this.validSignature = validSignature;
    }

    public StageEnum getStage() {
        return this.stage;
    }

    public void setStage(StageEnum stage) {
        this.stage = stage;
    }

    public String toString() {
        return "Result{valid=" + this.valid + ", error=" + this.error + ", validQrCode=" + this.validQrCode + ", validSignature=" + this.validSignature + "}";
    }
}

