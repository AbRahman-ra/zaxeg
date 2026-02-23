/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig;

import java.util.List;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;

public interface XMLSignature
extends XMLStructure {
    public static final String XMLNS = "http://www.w3.org/2000/09/xmldsig#";

    public boolean validate(XMLValidateContext var1) throws XMLSignatureException;

    public KeyInfo getKeyInfo();

    public SignedInfo getSignedInfo();

    public List getObjects();

    public String getId();

    public SignatureValue getSignatureValue();

    public void sign(XMLSignContext var1) throws MarshalException, XMLSignatureException;

    public KeySelectorResult getKeySelectorResult();

    public static interface SignatureValue
    extends XMLStructure {
        public String getId();

        public byte[] getValue();

        public boolean validate(XMLValidateContext var1) throws XMLSignatureException;
    }
}

