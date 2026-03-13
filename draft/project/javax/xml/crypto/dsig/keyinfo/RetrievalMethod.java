/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.keyinfo;

import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;

public interface RetrievalMethod
extends URIReference,
XMLStructure {
    public List getTransforms();

    public String getURI();

    public Data dereference(XMLCryptoContext var1) throws URIReferenceException;
}

