/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.generation.service;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.xml.sax.SAXException;

public interface HashingGenerationService {
    public String getInvoiceHash(String var1) throws ParserConfigurationException, TransformerException, IOException, SAXException, InvalidCanonicalizerException, CanonicalizationException;
}

