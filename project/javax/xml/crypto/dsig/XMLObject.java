/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.util.List;
import javax.xml.crypto.XMLStructure;

public interface XMLObject
extends XMLStructure {
    public static final String TYPE = "http://www.w3.org/2000/09/xmldsig#Object";

    public List getContent();

    public String getId();

    public String getMimeType();

    public String getEncoding();
}

