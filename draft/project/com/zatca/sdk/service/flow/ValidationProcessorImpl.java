/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.flow;

import com.zatca.sdk.service.flow.ValidationProcessor;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import com.zatca.sdk.service.validation.pih.PihValidator;
import com.zatca.sdk.service.validation.qrcode.QrCodeValidator;
import com.zatca.sdk.service.validation.schematron.EnSchematronValidator;
import com.zatca.sdk.service.validation.schematron.KsaSchematronValidator;
import com.zatca.sdk.service.validation.signature.SignatureValidator;
import com.zatca.sdk.service.validation.xsd.XsdValidator;
import com.zatca.sdk.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.xml.sax.SAXException;

public class ValidationProcessorImpl
implements ValidationProcessor {
    private static final Logger LOG = Logger.getLogger(ValidationProcessorImpl.class);

    @Override
    public Result run(File invoice) throws IOException, ParserConfigurationException, TransformerException, SAXException, InvalidCanonicalizerException, CanonicalizationException {
        /**
         * 4 stages of validation
         */
        Result result = new Result(); // empty validation
        /**
         * STAGE 1: VALIDATE XSD
         */
        XsdValidator xsdValidator = new XsdValidator();
        Result xsdResult = xsdValidator.validate(invoice);
        this.logResult(xsdResult, "xsd");
        if (!this.mergeResults(result, xsdResult).isValid()) {
            result.setStage(StageEnum.XSD);
            return result;
        }
        /**
         * STAGE 2: VALIDATE ENSCHEMATRON
         */
        EnSchematronValidator enSchematronValidator = new EnSchematronValidator();
        Result enResult = enSchematronValidator.validate(invoice);
        this.logResult(enResult, "en");
        this.mergeResults(result, enResult);
        if (!enResult.isValid()) {
            result.setStage(StageEnum.EN);
            return enResult;
        }
        /**
         * STAGE 3: VALIDATE KSA SCHEMATRON
         */
        KsaSchematronValidator ksaSchematronValidator = new KsaSchematronValidator();
        Result ksaResult = ksaSchematronValidator.validate(invoice);
        this.logResult(ksaResult, "ksa");
        if (!this.mergeResults(result, ksaResult).isValid()) {
            result.setStage(StageEnum.KSA);
            return result;
        }
        if (Utils.isInvoiceSimplified(invoice)) {
            // if simplified invoice, validate qr and signature
            QrCodeValidator qrCodeValidator = new QrCodeValidator();
            Result qrCodeResult = qrCodeValidator.validate(invoice);
            this.logResult(qrCodeResult, "qr");
            if (!this.mergeResults(result, qrCodeResult).isValid()) {
                result.setStage(StageEnum.QR);
                return result;
            }
            SignatureValidator signatureValidator = new SignatureValidator();
            Result signatureValidatorResult = signatureValidator.validate(invoice);
            signatureValidatorResult.setValidQrCode(qrCodeResult.isValid());
            this.logResult(signatureValidatorResult, "signature");
            if (!this.mergeResults(result, signatureValidatorResult).isValid()) {
                result.setStage(StageEnum.SIGNATURE);
                return result;
            }
        }
        /**
         * STAGE 4: VALIDE PIH
         */
        PihValidator pihValidator = new PihValidator();
        Result pihResult = pihValidator.validate(invoice);
        this.logResult(pihResult, "pih");
        this.mergeResults(result, pihResult);
        result.setStage(StageEnum.PIH);
        // no need for if statement because the last merge will decide if it's valid or not
        return result;
    }

    /**
     * Logging helper
     * @param result
     * @param stage
     */
    private void logResult(Result result, String stage) {
        LOG.info("[" + stage.toUpperCase() + "] validation result : " + (result.isValid() ? "PASSED" : "FAILED"));
        if (!result.isValid()) {
            LOG.error(stage + " validation errors : ");
            for (Map.Entry<String, String> entry : result.getError().entrySet()) {
                String code = entry.getKey();
                String message = entry.getValue();
                LOG.error("CODE : " + code + ", MESSAGE : " + message);
            }
        }
    }

    private Result mergeResults(Result initial, Result toBeMerged) {
        HashMap<String, String> errors = new HashMap<String, String>();
        if (toBeMerged.getError() != null) {
            errors.putAll(toBeMerged.getError());
            initial.setError(errors);
        }
        initial.setValid(initial.isValid() && toBeMerged.isValid());
        switch (toBeMerged.getStage()) {
            case QR: {
                initial.setValidQrCode(initial.isValidQrCode() || toBeMerged.isValidQrCode());
                break;
            }
            case SIGNATURE: {
                initial.setValidSignature(initial.isValidSignature() || toBeMerged.isValidSignature());
                break;
            }
        }
        return initial;
    }
}

