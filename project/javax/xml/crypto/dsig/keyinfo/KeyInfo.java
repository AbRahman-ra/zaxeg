/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.keyinfo;

import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;

public interface KeyInfo
extends XMLStructure {
    public List getContent();

    public String getId();

    public void marshal(XMLStructure var1, XMLCryptoContext var2) throws MarshalException;
}

