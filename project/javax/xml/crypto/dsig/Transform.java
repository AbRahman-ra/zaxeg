/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.Data;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.TransformException;

public interface Transform
extends XMLStructure,
AlgorithmMethod {
    public static final String BASE64 = "http://www.w3.org/2000/09/xmldsig#base64";
    public static final String ENVELOPED = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
    public static final String XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public static final String XPATH2 = "http://www.w3.org/2002/06/xmldsig-filter2";
    public static final String XSLT = "http://www.w3.org/TR/1999/REC-xslt-19991116";

    public AlgorithmParameterSpec getParameterSpec();

    public Data transform(Data var1, XMLCryptoContext var2) throws TransformException;

    public Data transform(Data var1, XMLCryptoContext var2, OutputStream var3) throws TransformException;
}

