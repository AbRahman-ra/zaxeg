/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.XMLStructure;

public interface SignatureMethod
extends XMLStructure,
AlgorithmMethod {
    public static final String DSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
    public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";

    public AlgorithmParameterSpec getParameterSpec();
}

