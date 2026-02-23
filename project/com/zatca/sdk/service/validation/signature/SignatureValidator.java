/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation.signature;

import com.gazt.einvoicing.hashing.generation.service.HashingGenerationService;
import com.gazt.einvoicing.hashing.generation.service.impl.HashingGenerationServiceImpl;
import com.zatca.config.ResourcesPaths;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import com.zatca.sdk.service.validation.Validator;
import com.zatca.sdk.util.ECDSAUtil;
import com.zatca.sdk.util.EcryptionUtils;
import com.zatca.sdk.util.Utils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class SignatureValidator
implements Validator {
    private static final Logger LOG = Logger.getLogger(SignatureValidator.class);
    private static final HashingGenerationService HASHING_GENERATION_SERVICE = new HashingGenerationServiceImpl();
    private static ResourcesPaths paths = ResourcesPaths.getInstance();

    @Override
    public Result validate(File in) {
        Result result = new Result();
        result.setStage(StageEnum.SIGNATURE);
        if (Utils.isInvoiceSimplified(in)) {
            return this.doValidate(in);
        }
        return result;
    }

    private Result doValidate(File in) {
        Result result = new Result();
        result.setStage(StageEnum.SIGNATURE);
        HashMap<String, String> errors = new HashMap<String, String>();
        try {
            String signingCertificateDigestValueCalculated;
            String signingCertificateDigestValue;
            String invoiceSignedDataDigestValue;
            String content = FileUtils.readFileToString(in, "UTF-8");
            String xmlHashing = HASHING_GENERATION_SERVICE.getInvoiceHash(content);
            String certificate = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='KeyInfo']//*[local-name()='X509Data']//*[local-name()='X509Certificate']");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate.getBytes(StandardCharsets.UTF_8)));
            CertificateFactory certificatefactory = null;
            try {
                certificatefactory = CertificateFactory.getInstance("X.509");
            } catch (CertificateException e) {
                LOG.error(e);
                result.setValid(false);
                errors.put("certificate", "wrong invoiceCertificate  ");
            }
            X509Certificate x509Certificate = null;
            try {
                x509Certificate = (X509Certificate)certificatefactory.generateCertificate(byteArrayInputStream);
            } catch (CertificateException e) {
                LOG.error(e);
                result.setValid(false);
                errors.put("certificate", "wrong invoiceCertificate  ");
            }
            String signatureValue = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='SignatureValue']");
            if (!ECDSAUtil.verifyECDSA(x509Certificate.getPublicKey(), signatureValue, Base64.getDecoder().decode(xmlHashing.getBytes(StandardCharsets.UTF_8)))) {
                result.setValid(false);
                errors.put("signatureValue", "wrong signature Value ");
            }
            if (!(invoiceSignedDataDigestValue = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='SignedInfo']//*[local-name()='Reference']//*[local-name()='DigestValue']")).equals(xmlHashing)) {
                LOG.debug(xmlHashing + "<vs>" + invoiceSignedDataDigestValue);
                result.setValid(false);
                errors.put("invoiceSignedDataDigestValue", "wrong invoice hashing");
            }
            String SIGNED_PROPERTIES = "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties";
            String xadesSignedPropertiesDigestValueCalculated = Utils.getNodeXmlValue(content, SIGNED_PROPERTIES);
            String xadesSignedPropertiesDigestValue = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='SignedInfo']//*[local-name()='Reference'][2]//*[local-name()='DigestValue']");
            String xadesSignedPropertiesDigestValueHashing = Base64.getEncoder().encodeToString(EcryptionUtils.hashString(xadesSignedPropertiesDigestValueCalculated).getBytes(StandardCharsets.UTF_8));
            if (!xadesSignedPropertiesDigestValueHashing.equals(xadesSignedPropertiesDigestValue)) {
                LOG.debug(xadesSignedPropertiesDigestValueHashing + "<vs>" + invoiceSignedDataDigestValue);
                result.setValid(false);
                errors.put("xadesSignedPropertiesDigestValue", "wrong xadesSignedPropertiesDigestValue  ");
            }
            if (!(signingCertificateDigestValue = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='Object']//*[local-name()='QualifyingProperties']//*[local-name()='SignedProperties']//*[local-name()='SignedSignatureProperties']//*[local-name()='SigningCertificate']//*[local-name()='Cert']//*[local-name()='CertDigest']//*[local-name()='DigestValue']")).equals(signingCertificateDigestValueCalculated = SignatureValidator.getHashedCertificate(certificate))) {
                LOG.debug(signingCertificateDigestValue + "<vs>" + signingCertificateDigestValueCalculated);
                result.setValid(false);
                errors.put("signingCertificateDigestValue", "wrong signingCertificateDigestValue  ");
            }
            String X509IssuerName = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='Object']//*[local-name()='QualifyingProperties']//*[local-name()='SignedProperties']//*[local-name()='SignedSignatureProperties']//*[local-name()='SigningCertificate']//*[local-name()='Cert']//*[local-name()='IssuerSerial']//*[local-name()='X509IssuerName']");
            String X509IssuerNameFromCertificate = this.getX509IssuerName();
            if (!X509IssuerName.trim().equals(X509IssuerNameFromCertificate.trim())) {
                LOG.debug(X509IssuerName + "<vs>" + X509IssuerNameFromCertificate);
                result.setValid(false);
                errors.put("X509IssuerName", "wrong X509IssuerName  ");
            }
            String X509SerialNumber = Utils.getNodeContentXpth(in, "//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='UBLDocumentSignatures']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='Object']//*[local-name()='QualifyingProperties']//*[local-name()='SignedProperties']//*[local-name()='SignedSignatureProperties']//*[local-name()='SigningCertificate']//*[local-name()='Cert']//*[local-name()='IssuerSerial']//*[local-name()='X509SerialNumber']");
            BigInteger X509SerialNumberFromCertificate = this.getX509SerialNumber();
            if (!X509SerialNumberFromCertificate.equals(new BigInteger(X509SerialNumber))) {
                LOG.debug(X509SerialNumberFromCertificate + "<vs>" + X509SerialNumber);
                result.setValid(false);
                errors.put("X509SerialNumber", "wrong X509SerialNumber  ");
            }
        } catch (Exception e) {
            result.setValid(false);
            errors.put(e.getClass().getSimpleName(), e.getMessage());
            LOG.error("Error : " + e.getMessage());
        }
        result.setError(errors);
        if (result.isValid()) {
            result.setValidSignature(true);
        }
        return result;
    }

    public static String getHashedCertificate(String certificate) throws Exception {
        byte[] hashedCert = EcryptionUtils.hashString(certificate.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(SignatureValidator.bytesToHex(hashedCert).getBytes(StandardCharsets.UTF_8));
    }

    private static X509Certificate getX509Certificate() throws IOException, CertificateException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(IOUtils.toString(new File(paths.getCertificatePath()).toURI(), StandardCharsets.UTF_8)));
        CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate)certificatefactory.generateCertificate(byteArrayInputStream);
    }

    public BigInteger getX509SerialNumber() throws Exception {
        X509Certificate certx509 = SignatureValidator.getX509Certificate();
        return certx509.getSerialNumber();
    }

    public String getX509IssuerName() throws Exception {
        X509Certificate certx509 = SignatureValidator.getX509Certificate();
        return certx509.getIssuerDN().getName();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

