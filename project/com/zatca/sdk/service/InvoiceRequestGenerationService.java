/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.zatca.sdk.service.GeneratorTemplate;
import com.zatca.sdk.util.XmlUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class InvoiceRequestGenerationService
extends GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(InvoiceRequestGenerationService.class);
    private String requestStr;
    private static final String UUID_XPATH_EXPRESSION = "/Invoice/UUID";
    private static final String INVOICE_HASH_XPATH_EXPRESSION = "/Invoice/UBLExtensions/UBLExtension/ExtensionContent/UBLDocumentSignatures/SignatureInformation/Signature/SignedInfo/Reference/DigestValue";

    public Properties getInvoiceData(String invoice) {
        try {
            LOG.debug(" get invoice data");
            Document document = XmlUtil.transform(invoice);
            String uuidValue = "";
            NodeList uuidNodeList = XmlUtil.evaluateXpath(document, UUID_XPATH_EXPRESSION);
            if (uuidNodeList != null) {
                uuidValue = uuidNodeList.item(0).getFirstChild().getNodeValue();
            }
            NodeList invoiceHashNodeList = XmlUtil.evaluateXpath(document, INVOICE_HASH_XPATH_EXPRESSION);
            String invoiceHashValue = "";
            if (invoiceHashNodeList != null && invoiceHashNodeList.getLength() > 0) {
                invoiceHashValue = invoiceHashNodeList.item(0).getFirstChild().getNodeValue();
            }
            Properties prop = new Properties();
            prop.put("invoiceHash", invoiceHashValue);
            prop.put("uuid", uuidValue);
            prop.put("invoice", invoice);
            return prop;
        } catch (Exception e) {
            LOG.error("failed to generate invoice request - " + e.getMessage());
            return null;
        }
    }

    public String prepareRequestBodyString(Properties prop) {
        LOG.debug("prepare request body");
        String jsonValue = String.format("{\"invoiceHash\":\"%s\",\"uuid\":\"%s\",\"invoice\":\"%s\"}", prop.getProperty("invoiceHash"), prop.getProperty("uuid"), Base64.getEncoder().encodeToString(prop.getProperty("invoice").getBytes()));
        return jsonValue;
    }

    public boolean prepareRequestBody(Properties prop) {
        try {
            LOG.debug("prepare request JSON file");
            this.requestStr = this.prepareRequestBodyString(prop);
            return this.requestStr != null;
        } catch (Exception e) {
            LOG.error("failed to generate invoice request - " + e.getMessage());
            return false;
        }
    }

    private Boolean generateInvoiceRequestFile() {
        try {
            LOG.debug("generate invoice request file ");
            this.generateFile(this.property.getInvoiceRequestFileName(), this.requestStr);
            LOG.info("invoice request has been generated successfully");
            return true;
        } catch (Exception e) {
            LOG.error("failed to generate invoice request [unable to write the invoice request into file] ");
            return false;
        }
    }

    @Override
    protected boolean loadInvoiceFile() {
        try {
            LOG.debug("load invoice file");
            this.invoiceStr = this.property.isGenerateSignature() ? FileUtils.readFileToString(new File(this.property.getOutputInvoiceFileName()), StandardCharsets.UTF_8) : FileUtils.readFileToString(new File(this.property.getInvoiceFileName()), StandardCharsets.UTF_8);
        } catch (IOException e1) {
            LOG.error("unable to read the invoice file content");
            return false;
        }
        return true;
    }

    @Override
    public boolean loadInput() {
        return this.loadInvoiceFile();
    }

    @Override
    public boolean validateInput() {
        return true;
    }

    @Override
    public boolean process() {
        Properties prop = this.getInvoiceData(this.invoiceStr);
        if (prop == null) {
            return false;
        }
        return this.prepareRequestBody(prop);
    }

    @Override
    public boolean generateOutput() {
        return this.generateInvoiceRequestFile();
    }
}

