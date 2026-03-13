/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.math.BigInteger;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public final class DOMCryptoBinary
extends DOMStructure {
    private final BigInteger bigNum;
    private final String value;

    public DOMCryptoBinary(BigInteger bigNum) {
        if (bigNum == null) {
            throw new NullPointerException("bigNum is null");
        }
        this.bigNum = bigNum;
        this.value = Base64.encode(bigNum);
    }

    public DOMCryptoBinary(Node cbNode) throws MarshalException {
        this.value = cbNode.getNodeValue();
        try {
            this.bigNum = Base64.decodeBigIntegerFromText((Text)cbNode);
        } catch (Exception ex) {
            throw new MarshalException(ex);
        }
    }

    public BigInteger getBigNum() {
        return this.bigNum;
    }

    public void marshal(Node parent, String prefix, DOMCryptoContext context) throws MarshalException {
        parent.appendChild(DOMUtils.getOwnerDocument(parent).createTextNode(this.value));
    }
}

