/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk;

import com.zatca.config.Config;
import com.zatca.config.ResourcesPaths;
import com.zatca.configuration.enums.Configuration;
import com.zatca.sdk.dto.ApplicationPropertyDto;
import com.zatca.sdk.service.ArgumentHandlerService;
import com.zatca.sdk.service.CsrGenerationService;
import com.zatca.sdk.service.GeneratorTemplate;
import com.zatca.sdk.service.HashGenerationService;
import com.zatca.sdk.service.InvoiceRequestGenerationService;
import com.zatca.sdk.service.InvoiceSigningService;
import com.zatca.sdk.service.InvoiceValidationService;
import com.zatca.sdk.service.QrGenerationService;
import org.apache.log4j.Logger;

public class MainApp {
    private static final Logger LOG = Logger.getLogger(MainApp.class);
    private static ResourcesPaths paths;

    public static void main(String[] args) {
        System.out.println("********** Welcome to ZATCA E-Invoice Java SDK 3.0.8 *********************\r\nThis SDK uses Java to call the SDK (jar) passing it an invoice XML file.\r\nIt can take a Standard or Simplified XML, Credit Note, or Debit Note.\r\nIt returns if the validation is successful or shows errors where the XML validation fails.\r\nIt checks for syntax and content as well.\r\n\r\n****************************************************************");
        ArgumentHandlerService argumentHandler = new ArgumentHandlerService();
        ApplicationPropertyDto applicationProperty = argumentHandler.loadGlobalSetting(args);
        if (applicationProperty.isGenerateCsr()) {
            CsrGenerationService generateService = new CsrGenerationService();
            generateService.generate(applicationProperty);
            return;
        }
        if (applicationProperty.isGenerateSignature()) {
            GeneratorTemplate generateService = new InvoiceSigningService();
            generateService.generate(applicationProperty);
            if (applicationProperty.isGenerateInvoiceRequest()) {
                generateService = new InvoiceRequestGenerationService();
                generateService.generate(applicationProperty);
            }
            return;
        }
        if (applicationProperty.isGenerateInvoiceRequest()) {
            InvoiceRequestGenerationService generateService = new InvoiceRequestGenerationService();
            generateService.generate(applicationProperty);
            return;
        }
        if (applicationProperty.isGenerateHash()) {
            HashGenerationService generateService = new HashGenerationService();
            generateService.generate(applicationProperty);
            return;
        }
        if (applicationProperty.isGenerateQr()) {
            QrGenerationService generateService = new QrGenerationService();
            generateService.generate(applicationProperty);
            return;
        }
        if (applicationProperty.isValidateInvoice()) {
            InvoiceValidationService generateService = new InvoiceValidationService();
            generateService.generate(applicationProperty);
            return;
        }
    }

    static {
        try {
            new Config();
            paths = Config.readResourcesPaths();
            if (Configuration.getInstance().getStampCertificatePath() != null) {
                paths.setCertificatePath(Configuration.getInstance().getStampCertificatePath());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}

