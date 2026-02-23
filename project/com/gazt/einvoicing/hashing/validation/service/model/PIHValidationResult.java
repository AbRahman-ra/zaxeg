/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.validation.service.model;

import com.gazt.einvoicing.hashing.validation.service.model.ValidationPIHItem;
import java.util.ArrayList;
import java.util.List;

public class PIHValidationResult {
    private boolean valid;
    private List<ValidationPIHItem> validationPIHItemList = new ArrayList<ValidationPIHItem>();

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationPIHItem> getValidationPIHItemList() {
        return this.validationPIHItemList;
    }

    public void setValidationPIHItemList(List<ValidationPIHItem> validationPIHItemList) {
        this.validationPIHItemList = validationPIHItemList;
    }
}

