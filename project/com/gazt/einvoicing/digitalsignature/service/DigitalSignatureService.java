/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.digitalsignature.service;

import com.gazt.einvoicing.digitalsignature.service.model.DigitalSignature;
import java.security.PrivateKey;
import javax.xml.stream.XMLStreamException;
import org.dom4j.DocumentException;

public interface DigitalSignatureService {
    public DigitalSignature getDigitalSignature(String var1, PrivateKey var2, String var3) throws DocumentException, XMLStreamException;
}

