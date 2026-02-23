/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.digitalsignature.service.impl;

import com.gazt.einvoicing.digitalsignature.service.DigitalSignatureService;
import com.gazt.einvoicing.digitalsignature.service.model.DigitalSignature;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DigitalSignatureServiceImpl
implements DigitalSignatureService {
    private static final Logger LOG = Logger.getLogger(DigitalSignatureServiceImpl.class.getName());

    @Override
    public DigitalSignature getDigitalSignature(String xmlDocument, PrivateKey privateKey, String xmlHashing) {
        byte[] xmlHashingBytes = Base64.getDecoder().decode(xmlHashing.getBytes(StandardCharsets.UTF_8));
        byte[] digitalSignatureBytes = this.signECDSA(privateKey, xmlHashingBytes);
        DigitalSignature digitalSignature = new DigitalSignature();
        digitalSignature.setDigitalSignature(Base64.getEncoder().encodeToString(digitalSignatureBytes));
        digitalSignature.setXmlHashing(xmlHashingBytes);
        return digitalSignature;
    }

    byte[] signECDSA(PrivateKey privateKey, byte[] messageHash) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(messageHash);
            byte[] sign = signature.sign();
            return sign;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return null;
        }
    }
}

