/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.io.InputStream;
import java.util.List;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.SignatureMethod;

public interface SignedInfo
extends XMLStructure {
    public CanonicalizationMethod getCanonicalizationMethod();

    public SignatureMethod getSignatureMethod();

    public List getReferences();

    public String getId();

    public InputStream getCanonicalizedData();
}

