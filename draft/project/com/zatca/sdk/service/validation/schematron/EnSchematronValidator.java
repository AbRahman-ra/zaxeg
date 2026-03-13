/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation.schematron;

import com.zatca.config.ResourcesPaths;
import com.zatca.sdk.service.validation.schematron.SchematronValidator;

public class EnSchematronValidator
extends SchematronValidator {
    private ResourcesPaths paths = ResourcesPaths.getInstance();

    @Override
    protected String getSchematronPath() {
        return this.paths.getEnSchematronPath();
    }
}

