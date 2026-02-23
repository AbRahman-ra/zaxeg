/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.gazt.einvoicing.hashing.generation.service.HashingGenerationService;
import com.gazt.einvoicing.hashing.generation.service.impl.HashingGenerationServiceImpl;
import com.gazt.einvoicing.qr.generation.service.QRCodeGeneratorService;
import com.gazt.einvoicing.qr.generation.service.impl.QRCodeGeneratorServiceImpl;
import com.zatca.sdk.service.GeneratorTemplate;
import com.zatca.sdk.util.XmlUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QrGenerationService
extends GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(QrGenerationService.class);
    private String qrCode;
    private QRCodeGeneratorService qrCodeGeneratorService = new QRCodeGeneratorServiceImpl();
    private HashingGenerationService hashingGenerationService = new HashingGenerationServiceImpl();
    private static final String SELLER_NAME_XPATH = "/Invoice/AccountingSupplierParty/Party/PartyLegalEntity/RegistrationName";
    private static final String VAT_REGISTERATION_XPATH = "/Invoice/AccountingSupplierParty/Party/PartyTaxScheme/CompanyID";
    private static final String ISSUE_DATE_XPATH = "/Invoice/IssueDate";
    private static final String ISSUE_TIME_XPATH = "/Invoice/IssueTime";
    private static final String INVOICE_TOTAL_XPATH = "/Invoice/LegalMonetaryTotal/TaxInclusiveAmount";
    private static final String VAT_TOTAL_XPATH = "/Invoice/TaxTotal/TaxAmount";
    private static final String SIGNATURE_XPATH = "/Invoice/UBLExtensions/UBLExtension/ExtensionContent/UBLDocumentSignatures/SignatureInformation/Signature/SignatureValue";
    private static final String CERTIFICATE_XPATH = "/Invoice/UBLExtensions/UBLExtension/ExtensionContent/UBLDocumentSignatures/SignatureInformation/Signature/KeyInfo/X509Data/X509Certificate";
    private static final String QR_CODE_XPATH = "/Invoice/AdditionalDocumentReference[ID[text()='QR']]/Attachment/EmbeddedDocumentBinaryObject";

    public String generateQrCode(String xmlContent) {
        String certificate;
        String signature;
        String vatTotal;
        String invoiceTotal;
        String timeStamp;
        String vatRegistrationNumber;
        Map<String, String> values = this.getQrDataFromInvoice(xmlContent);
        if (values == null) {
            return null;
        }
        String sellerName = values.get("sellerName");
        boolean validateResult = QrGenerationService.validateQrInputData(sellerName, vatRegistrationNumber = values.get("vatRegistrationNumber"), timeStamp = values.get("timeStamp"), invoiceTotal = values.get("invoiceTotal"), vatTotal = values.get("vatTotal"), signature = values.get("signature"), certificate = values.get("certificate"));
        if (!validateResult) {
            return null;
        }
        X509Certificate x509Certificate = this.extractCertificate(certificate);
        if (x509Certificate == null) {
            return null;
        }
        String hashedXml = this.getInvoiceHash(xmlContent);
        if (hashedXml == null) {
            return null;
        }
        return this.generateQr(sellerName, vatRegistrationNumber, timeStamp, invoiceTotal, vatTotal, signature, x509Certificate, hashedXml);
    }

    private String generateQr(String sellerName, String vatRegistrationNumber, String timeStamp, String invoiceTotal, String vatTotal, String signature, X509Certificate x509Certificate, String hashedXml) {
        try {
            return this.qrCodeGeneratorService.generateQrCode(sellerName, vatRegistrationNumber, timeStamp, invoiceTotal, vatTotal, hashedXml, x509Certificate.getPublicKey().getEncoded(), signature);
        } catch (Exception e1) {
            LOG.error("failed to generate qr - " + e1.getMessage());
            return null;
        }
    }

    public String getInvoiceHash(String xmlContent) {
        try {
            String hashedXml = this.hashingGenerationService.getInvoiceHash(xmlContent);
            return hashedXml;
        } catch (InvalidCanonicalizerException e1) {
            LOG.error("failed to generate qr [" + e1.getMessage() + "]");
            return null;
        } catch (CanonicalizationException e1) {
            LOG.error("failed to generate qr [" + e1.getMessage() + "]");
            return null;
        } catch (ParserConfigurationException e1) {
            LOG.error("failed to generate qr [" + e1.getMessage() + "]");
            return null;
        } catch (TransformerException e1) {
            LOG.error("failed to generate qr [unable to transform xml file content] - " + e1.getMessage());
            return null;
        } catch (IOException e1) {
            LOG.error("failed to generate qr [unable to read xml file content] - " + e1.getMessage());
            return null;
        } catch (SAXException e1) {
            LOG.error("failed to generate qr [invalid xml document] - " + e1.getMessage());
            return null;
        }
    }

    private X509Certificate extractCertificate(String certificate) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate.getBytes(StandardCharsets.UTF_8)));
            CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
            X509Certificate x509Certificate = (X509Certificate)certificatefactory.generateCertificate(byteArrayInputStream);
            return x509Certificate;
        } catch (CertificateException e1) {
            LOG.error("failed to generate qr [invalid certificate]");
            return null;
        }
    }

    private static boolean validateQrInputData(String sellerName, String vatRegistrationNumber, String timeStamp, String invoiceTotal, String vatTotal, String signature, String certificate) {
        if (sellerName == null || sellerName.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get seller name from invoice]");
            return false;
        }
        if (vatRegistrationNumber == null || vatRegistrationNumber.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get vat number from invoice]");
            return false;
        }
        if (timeStamp == null || timeStamp.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get timeStamp from invoice]");
            return false;
        }
        if (invoiceTotal == null || invoiceTotal.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get invoiceTotal from invoice]");
            return false;
        }
        if (vatTotal == null || vatTotal.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get vatTotal from invoice]");
            return false;
        }
        if (signature == null || signature.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get signature from invoice]");
            return false;
        }
        if (certificate == null || certificate.trim().isEmpty()) {
            LOG.error("failed to generate qr [unable to get certificate from invoice]");
            return false;
        }
        return true;
    }

    public Map<String, String> getQrDataFromInvoice(String xmlDocumentStr) {
        HashMap<String, String> values = new HashMap<String, String>();
        Document doc = XmlUtil.transform(xmlDocumentStr);
        try {
            String sellerNameValue = "";
            NodeList sellerNamedNodeList = XmlUtil.evaluateXpath(doc, SELLER_NAME_XPATH);
            if (sellerNamedNodeList != null) {
                sellerNameValue = sellerNamedNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("sellerName", sellerNameValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get seller name from invoice]");
            return null;
        }
        try {
            String vatRegistrationNumberValue = "";
            NodeList vatRegistrationNumberNodeList = XmlUtil.evaluateXpath(doc, VAT_REGISTERATION_XPATH);
            if (vatRegistrationNumberNodeList != null) {
                vatRegistrationNumberValue = vatRegistrationNumberNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("vatRegistrationNumber", vatRegistrationNumberValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get vat number from invoice]");
            return null;
        }
        String issueDateValue = "";
        try {
            NodeList issueDateNodeList = XmlUtil.evaluateXpath(doc, ISSUE_DATE_XPATH);
            if (issueDateNodeList != null) {
                issueDateValue = issueDateNodeList.item(0).getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get issue date from invoice]");
            return null;
        }
        String issueTimeValue = "";
        try {
            NodeList issueTimeNodeList = XmlUtil.evaluateXpath(doc, ISSUE_TIME_XPATH);
            if (issueTimeNodeList != null) {
                issueTimeValue = issueTimeNodeList.item(0).getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get issue time from invoice]");
            return null;
        }
        try {
            if (!issueDateValue.isEmpty() && !issueTimeValue.isEmpty()) {
                Date oldFormatDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(issueDateValue + " " + issueTimeValue);
                String newFormatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(oldFormatDate).toString();
                values.put("timeStamp", newFormatDate);
            }
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to convert issue date time]");
            return null;
        }
        try {
            String invoiceTotalValue = "";
            NodeList invoiceTotalNodeList = XmlUtil.evaluateXpath(doc, INVOICE_TOTAL_XPATH);
            if (invoiceTotalNodeList != null) {
                invoiceTotalValue = invoiceTotalNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("invoiceTotal", invoiceTotalValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get invoice total from invoice]");
            return null;
        }
        try {
            String vatTotalValue = "";
            NodeList vatTotalNodeList = XmlUtil.evaluateXpath(doc, VAT_TOTAL_XPATH);
            if (vatTotalNodeList != null) {
                vatTotalValue = vatTotalNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("vatTotal", vatTotalValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get vat total from invoice]");
            return null;
        }
        try {
            String signatureValue = "";
            NodeList signatureNodeList = XmlUtil.evaluateXpath(doc, SIGNATURE_XPATH);
            if (signatureNodeList != null) {
                signatureValue = signatureNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("signature", signatureValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get signature from invoice]");
            return null;
        }
        try {
            String certificateValue = "";
            NodeList certificateNodeList = XmlUtil.evaluateXpath(doc, CERTIFICATE_XPATH);
            if (certificateNodeList != null) {
                certificateValue = certificateNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("certificate", certificateValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get certificate from invoice]");
            return null;
        }
        try {
            String qrCodeValue = "";
            NodeList qrCodeNodeList = XmlUtil.evaluateXpath(doc, QR_CODE_XPATH);
            if (qrCodeNodeList != null) {
                qrCodeValue = qrCodeNodeList.item(0).getFirstChild().getNodeValue();
            }
            values.put("qrCode", qrCodeValue);
        } catch (Exception e) {
            LOG.error("failed to generate qr [unable to get qr code from invoice]");
            return null;
        }
        return values;
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
        this.qrCode = this.generateQrCode(this.invoiceStr);
        return this.qrCode != null;
    }

    @Override
    public boolean generateOutput() {
        LOG.info("Qr has been generated successfully");
        LOG.info(" *** QR code = " + this.qrCode);
        return true;
    }
}

