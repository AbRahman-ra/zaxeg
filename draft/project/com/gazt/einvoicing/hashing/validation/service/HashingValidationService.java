/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.validation.service;

import com.gazt.einvoicing.hashing.validation.service.model.HashValidationResult;
import com.gazt.einvoicing.hashing.validation.service.model.PIHValidationResult;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

public interface HashingValidationService {
    public HashValidationResult validateEInvoiceHash(String var1) throws ParserConfigurationException, TransformerConfigurationException, IOException, SAXException;

    public PIHValidationResult validateEInvoiceHash(List<String> var1) throws TransformerException, ParserConfigurationException, IOException, SAXException, XPathExpressionException;
}

