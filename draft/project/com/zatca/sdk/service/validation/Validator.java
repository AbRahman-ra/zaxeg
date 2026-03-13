/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation;

import com.zatca.sdk.service.validation.Result;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.xml.sax.SAXException;

public interface Validator {
    public Result validate(File var1) throws IOException, ParserConfigurationException, TransformerException, SAXException, InvalidCanonicalizerException, CanonicalizationException;
}

