/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.qr.generation.service.impl;

import com.gazt.einvoicing.qr.generation.service.QRCodeGeneratorService;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlvBuilder;
import com.starkbank.ellipticcurve.Signature;
import com.starkbank.ellipticcurve.utils.ByteString;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class QRCodeGeneratorServiceImpl
implements QRCodeGeneratorService {
    @Override
    public String generateQrCode(String sellerName, String vatRegistrationNumber, String timeStamp, String invoiceTotal, String vatTotal, String hashedXml, byte[] publicKey, String digitalSignature) throws Exception {
        Signature signature = Signature.fromBase64(new ByteString(digitalSignature.getBytes(StandardCharsets.UTF_8)));
        byte[] r = signature.r.toByteArray();
        byte[] s = signature.s.toByteArray();
        byte[] bytes = new BerTlvBuilder().addText(new BerTag(1), sellerName, StandardCharsets.UTF_8).addText(new BerTag(2), vatRegistrationNumber).addText(new BerTag(3), timeStamp).addText(new BerTag(4), invoiceTotal).addText(new BerTag(5), vatTotal).addText(new BerTag(6), hashedXml).addBytes(new BerTag(7), publicKey).addBytes(new BerTag(8), r).addBytes(new BerTag(9), s).buildArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}

