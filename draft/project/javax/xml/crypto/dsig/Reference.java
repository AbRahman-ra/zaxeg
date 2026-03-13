/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.io.InputStream;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;

public interface Reference
extends URIReference,
XMLStructure {
    public List getTransforms();

    public DigestMethod getDigestMethod();

    public String getId();

    public byte[] getDigestValue();

    public byte[] getCalculatedDigestValue();

    public boolean validate(XMLValidateContext var1) throws XMLSignatureException;

    public Data getDereferencedData();

    public InputStream getDigestInputStream();
}

