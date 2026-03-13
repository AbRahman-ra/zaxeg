/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.generation.service.impl;

import com.gazt.einvoicing.hashing.generation.service.HashingGenerationService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

public class HashingGenerationServiceImpl
implements HashingGenerationService {
    private static final Logger LOG = Logger.getLogger(HashingGenerationServiceImpl.class.getName());

    @Override
    public String getInvoiceHash(String xmlDocument) throws ParserConfigurationException, TransformerException, IOException, SAXException, InvalidCanonicalizerException, CanonicalizationException {
        Transformer transformer = this.getTransformer();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult xmlOutput = new StreamResult(byteArrayOutputStream);
        transformer.transform(new StreamSource(new StringReader(xmlDocument)), xmlOutput);
        return Base64.getEncoder().encodeToString(this.hashStringToBytes(this.canonicalizeXml(byteArrayOutputStream.toByteArray())));
    }

    private String canonicalizeXml(byte[] xmlDocument) throws InvalidCanonicalizerException, CanonicalizationException, ParserConfigurationException, IOException, SAXException {
        Init.init();
        Canonicalizer canon = Canonicalizer.getInstance("http://www.w3.org/2006/12/xml-c14n11");
        return new String(canon.canonicalize(xmlDocument));
    }

    private Transformer getTransformer() throws TransformerConfigurationException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(new ClassPathResource("invoice.xsl").getInputStream()));
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "no");
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        return transformer;
    }

    private byte[] hashStringToBytes(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return hash;
    }
}

