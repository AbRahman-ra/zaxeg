/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.gazt.einvoicing.signing.service.SigningService;
import com.gazt.einvoicing.signing.service.impl.SigningServiceImpl;
import com.gazt.einvoicing.signing.service.model.InvoiceSigningResult;
import com.zatca.config.ResourcesPaths;
import com.zatca.sdk.dto.ApplicationPropertyDto;
import com.zatca.sdk.service.GeneratorTemplate;
import com.zatca.sdk.util.ECDSAUtil;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import org.apache.log4j.Logger;

public class InvoiceSigningService
extends GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(InvoiceSigningService.class);
    private PrivateKey privateKey;
    private String certificateStr;
    private InvoiceSigningResult invoiceSigningResult;
    private SigningService signingService = new SigningServiceImpl();
    private ResourcesPaths paths = ResourcesPaths.getInstance();

    private Boolean generateSignedInvoiceFile(ApplicationPropertyDto property) {
        try {
            LOG.debug("generate signed invoice file ");
            this.generateFile(property.getOutputInvoiceFileName(), this.invoiceSigningResult.getSingedXML());
            return true;
        } catch (Exception e) {
            LOG.error("failed to sign invoice [unable to write the signed content into file] ");
            return false;
        }
    }

    protected boolean validateInputFiles() {
        boolean result = this.validatePrivateKey();
        if (!result) {
            return false;
        }
        result = this.validateCertificate();
        return result;
    }

    private boolean validateCertificate() {
        FileInputStream certificate = null;
        try {
            certificate = new FileInputStream(this.paths.getCertificatePath());
        } catch (FileNotFoundException e1) {
            LOG.error("failed to sign invoice [certificate file is not found] ");
            return false;
        }
        try {
            this.certificateStr = new String(certificate.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("failed to sign invoice [unable to read certificate] ");
            return false;
        }
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byte[] certificateBytes = this.certificateStr.getBytes(StandardCharsets.UTF_8);
            byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificateBytes));
        } catch (Exception e) {
            LOG.error("failed to sign invoice [please provide certificate decoded base64 ");
            return false;
        }
        try {
            CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
            certificatefactory.generateCertificate(byteArrayInputStream);
        } catch (Exception e) {
            LOG.error("failed to sign invoice [please provide a valid certificate] ");
            return false;
        }
        return true;
    }

    private boolean validatePrivateKey() {
        FileInputStream privateKey = null;
        try {
            privateKey = new FileInputStream(this.paths.getPrivateKeyPath());
        } catch (FileNotFoundException e1) {
            LOG.error("failed to sign invoice [private key file is not found] ");
            return false;
        }
        String key = null;
        try {
            key = new String(privateKey.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("failed to sign invoice [unable to read private key] ");
            return false;
        }
        if (key.contains("-----BEGIN EC PRIVATE KEY-----") || key.contains("-----END EC PRIVATE KEY-----")) {
            LOG.error("failed to sign invoice [please provide private key without -----BEGIN EC PRIVATE KEY----- and -----END EC PRIVATE KEY-----] ");
            return false;
        }
        try {
            this.privateKey = ECDSAUtil.getPrivateKey(key);
        } catch (Exception e2) {
            try {
                this.privateKey = ECDSAUtil.loadPrivateKey(key);
            } catch (Exception e) {
                LOG.error("failed to sign invoice [please provide a valid private key] ");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean signInvoice() {
        try {
            this.invoiceSigningResult = this.signingService.signDocument(this.invoiceStr, this.privateKey, this.certificateStr, "");
        } catch (Exception e1) {
            LOG.error("failed to sign invoice [" + e1.getMessage() + "] ");
            return false;
        }
        return this.invoiceSigningResult != null;
    }

    @Override
    public boolean loadInput() {
        return this.loadInvoiceFile();
    }

    @Override
    public boolean validateInput() {
        return this.validateInputFiles();
    }

    @Override
    public boolean process() {
        return this.signInvoice();
    }

    @Override
    public boolean generateOutput() {
        if (!this.generateSignedInvoiceFile(this.property).booleanValue()) {
            return false;
        }
        LOG.info("invoice has been signed successfully");
        LOG.info(" *** INVOICE HASH = " + this.invoiceSigningResult.getInvoiceHash());
        return true;
    }
}

