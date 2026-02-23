/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation.qrcode;

import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlvParser;
import com.payneteasy.tlv.BerTlvs;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.ByteString;
import com.zatca.sdk.service.QrGenerationService;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import com.zatca.sdk.service.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.xml.sax.SAXException;

public class QrCodeValidator
implements Validator {
    private static final Logger LOG = Logger.getLogger(QrCodeValidator.class);
    private QrGenerationService qrGenerationService = new QrGenerationService();

    @Override
    public Result validate(File in) throws IOException, ParserConfigurationException, TransformerException, SAXException, InvalidCanonicalizerException, CanonicalizationException {
        Result result = new Result();
        result.setStage(StageEnum.QR);
        HashMap<String, String> errors = new HashMap<String, String>();
        String invoiceStr = FileUtils.readFileToString(in, StandardCharsets.UTF_8);
        Map<String, String> values = this.qrGenerationService.getQrDataFromInvoice(invoiceStr);
        String sellerName = values.get("sellerName");
        String vatRegistrationNumber = values.get("vatRegistrationNumber");
        String timeStamp = values.get("timeStamp");
        String invoiceTotal = values.get("invoiceTotal");
        String vatTotal = values.get("vatTotal");
        String signature = values.get("signature");
        String certificate = values.get("certificate");
        String qrCode = values.get("qrCode");
        BerTlvs tlvs = null;
        byte[] rValue = null;
        byte[] sValue = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(qrCode.getBytes(StandardCharsets.UTF_8));
            BerTlvParser parser = new BerTlvParser();
            tlvs = parser.parse(bytes);
            Signature signatureObj = Signature.fromBase64(new ByteString(signature.getBytes(StandardCharsets.UTF_8)));
            rValue = signatureObj.r.toByteArray();
            sValue = signatureObj.s.toByteArray();
        } catch (Exception e) {
            errors.put("QR-Code", "Invalid QR code format, Please follow the ZATCA QR code specifications");
            result.setError(errors);
            result.setValid(false);
            result.setValidQrCode(false);
            return result;
        }
        String hashedXml = this.qrGenerationService.getInvoiceHash(invoiceStr);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate.getBytes(StandardCharsets.UTF_8)));
        CertificateFactory certificatefactory = null;
        try {
            certificatefactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            LOG.error(e);
        }
        X509Certificate x509Certificate = null;
        try {
            x509Certificate = (X509Certificate)certificatefactory.generateCertificate(byteArrayInputStream);
        } catch (CertificateException e) {
            LOG.error(e);
        }
        byte[] publicKey = x509Certificate.getPublicKey().getEncoded();
        String sellerNameFromQrCode = tlvs.find(new BerTag(1)).getTextValue(StandardCharsets.UTF_8);
        String vatRegistrationNumberFromQrCode = tlvs.find(new BerTag(2)).getTextValue();
        String timeStampFromQrCode = tlvs.find(new BerTag(3)).getTextValue();
        String invoiceTotalFromQrCode = tlvs.find(new BerTag(4)).getTextValue();
        String vatTotalFromQrCode = tlvs.find(new BerTag(5)).getTextValue();
        String hashedXmlFromQrCode = tlvs.find(new BerTag(6)).getTextValue();
        byte[] keyFromQrCode = tlvs.find(new BerTag(7)).getBytesValue();
        byte[] rFromQrCode = tlvs.find(new BerTag(8)).getBytesValue();
        byte[] sFromQrCode = tlvs.find(new BerTag(9)).getBytesValue();
        if (!sellerName.equals(sellerNameFromQrCode)) {
            result.setValid(false);
            errors.put("sellerName", "seller name does not match with qr code seller name");
        }
        if (!vatRegistrationNumber.equals(vatRegistrationNumberFromQrCode)) {
            result.setValid(false);
            errors.put("vatRegistrationNumber", "vat Registration Number does not match with qr code vat Registration Number");
        }
        if (!timeStamp.equals(timeStampFromQrCode)) {
            LOG.debug(timeStamp + "<vs>" + timeStampFromQrCode);
            result.setValid(false);
            errors.put("timeStamp", "timeStamp does not match with qr code timeStamp");
        }
        if (!invoiceTotal.equals(invoiceTotalFromQrCode)) {
            result.setValid(false);
            errors.put("invoiceTotal", "Invoice total in the invoice does not match with qr code invoice total");
        }
        if (!vatTotal.equals(vatTotalFromQrCode)) {
            result.setValid(false);
            errors.put("vatTotal", "vatTotal does not match with qr code vatTotal");
        }
        if (!Arrays.equals(publicKey, keyFromQrCode)) {
            result.setValid(false);
            errors.put("vatTotal", "vatTotal does not match with qr code vatTotal");
        }
        if (!Arrays.equals(rValue, rFromQrCode)) {
            result.setValid(false);
            errors.put("R", "R value of the signature tag in the invoice doesn't match the R value in tag 8 of the QR code");
        }
        if (!Arrays.equals(sValue, sFromQrCode)) {
            result.setValid(false);
            errors.put("S", "S value of the signature tag in the invoice doesn't match the S value in tag 9 of the QR code");
        }
        if (!hashedXml.equals(hashedXmlFromQrCode)) {
            LOG.debug(hashedXml + " <vs> " + hashedXmlFromQrCode);
            result.setValid(false);
            errors.put("hashedXml", "hashedXml does not match with qr code hashedXml");
        }
        result.setError(errors);
        if (result.isValid()) {
            result.setValidQrCode(true);
        }
        return result;
    }
}

