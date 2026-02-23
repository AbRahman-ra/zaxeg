/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.zatca.sdk.dto.ApplicationPropertyDto;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public abstract class GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(GeneratorTemplate.class);
    protected ApplicationPropertyDto property;
    protected String invoiceStr;

    protected abstract boolean loadInput();

    protected abstract boolean validateInput();

    protected abstract boolean process();

    protected abstract boolean generateOutput();

    public final boolean generate(ApplicationPropertyDto property) {
        this.property = property;
        boolean result = this.loadInput();
        if (!result) {
            return false;
        }
        result = this.validateInput();
        if (!result) {
            return false;
        }
        result = this.process();
        if (!result) {
            return false;
        }
        result = this.generateOutput();
        return result;
    }

    protected void generateFile(String fileName, String content) throws Exception {
        FileUtils.writeByteArrayToFile(new File(fileName), content.getBytes());
    }

    protected boolean loadInvoiceFile() {
        try {
            LOG.debug("load invoice file");
            this.invoiceStr = FileUtils.readFileToString(new File(this.property.getInvoiceFileName()), StandardCharsets.UTF_8);
        } catch (IOException e1) {
            LOG.error("unable to read the invoice file content");
            return false;
        }
        return true;
    }
}

