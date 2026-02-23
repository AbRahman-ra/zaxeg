/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.flow;

import com.zatca.sdk.service.validation.Result;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.xml.sax.SAXException;

public interface ValidationProcessor {
    public Result run(File var1) throws IOException, ParserConfigurationException, TransformerException, SAXException, InvalidCanonicalizerException, CanonicalizationException, CertificateException;
}

