/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.keyinfo;

import java.util.List;
import javax.xml.crypto.XMLStructure;

public interface X509Data
extends XMLStructure {
    public static final String TYPE = "http://www.w3.org/2000/09/xmldsig#X509Data";
    public static final String RAW_X509_CERTIFICATE_TYPE = "http://www.w3.org/2000/09/xmldsig#rawX509Certificate";

    public List getContent();
}

