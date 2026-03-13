/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import org.apache.jcp.xml.dsig.internal.dom.DOMStructure;
import org.apache.jcp.xml.dsig.internal.dom.DOMUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class DOMPGPData
extends DOMStructure
implements PGPData {
    private final byte[] keyId;
    private final byte[] keyPacket;
    private final List<XMLStructure> externalElements;

    public DOMPGPData(byte[] keyPacket, List<? extends XMLStructure> other) {
        if (keyPacket == null) {
            throw new NullPointerException("keyPacket cannot be null");
        }
        if (other == null || other.isEmpty()) {
            this.externalElements = Collections.emptyList();
        } else {
            this.externalElements = Collections.unmodifiableList(new ArrayList<XMLStructure>(other));
            int size = this.externalElements.size();
            for (int i = 0; i < size; ++i) {
                if (this.externalElements.get(i) instanceof XMLStructure) continue;
                throw new ClassCastException("other[" + i + "] is not a valid PGPData type");
            }
        }
        this.keyPacket = (byte[])keyPacket.clone();
        this.checkKeyPacket(keyPacket);
        this.keyId = null;
    }

    public DOMPGPData(byte[] keyId, byte[] keyPacket, List<? extends XMLStructure> other) {
        if (keyId == null) {
            throw new NullPointerException("keyId cannot be null");
        }
        if (keyId.length != 8) {
            throw new IllegalArgumentException("keyId must be 8 bytes long");
        }
        if (other == null || other.isEmpty()) {
            this.externalElements = Collections.emptyList();
        } else {
            this.externalElements = Collections.unmodifiableList(new ArrayList<XMLStructure>(other));
            int size = this.externalElements.size();
            for (int i = 0; i < size; ++i) {
                if (this.externalElements.get(i) instanceof XMLStructure) continue;
                throw new ClassCastException("other[" + i + "] is not a valid PGPData type");
            }
        }
        this.keyId = (byte[])keyId.clone();
        byte[] byArray = this.keyPacket = keyPacket == null ? null : (byte[])keyPacket.clone();
        if (keyPacket != null) {
            this.checkKeyPacket(keyPacket);
        }
    }

    public DOMPGPData(Element pdElem) throws MarshalException {
        byte[] keyId = null;
        byte[] keyPacket = null;
        ArrayList<javax.xml.crypto.dom.DOMStructure> other = new ArrayList<javax.xml.crypto.dom.DOMStructure>();
        for (Node firstChild = pdElem.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            if (firstChild.getNodeType() != 1) continue;
            Element childElem = (Element)firstChild;
            String localName = childElem.getLocalName();
            String namespace = childElem.getNamespaceURI();
            try {
                if (localName.equals("PGPKeyID") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                    keyId = Base64.decode(childElem);
                    continue;
                }
                if (localName.equals("PGPKeyPacket") && "http://www.w3.org/2000/09/xmldsig#".equals(namespace)) {
                    keyPacket = Base64.decode(childElem);
                    continue;
                }
                other.add(new javax.xml.crypto.dom.DOMStructure(childElem));
                continue;
            } catch (Base64DecodingException bde) {
                throw new MarshalException(bde);
            }
        }
        this.keyId = keyId;
        this.keyPacket = keyPacket;
        this.externalElements = Collections.unmodifiableList(other);
    }

    @Override
    public byte[] getKeyId() {
        return this.keyId == null ? null : (byte[])this.keyId.clone();
    }

    @Override
    public byte[] getKeyPacket() {
        return this.keyPacket == null ? null : (byte[])this.keyPacket.clone();
    }

    @Override
    public List getExternalElements() {
        return this.externalElements;
    }

    @Override
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element pdElem = DOMUtils.createElement(ownerDoc, "PGPData", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
        if (this.keyId != null) {
            Element keyIdElem = DOMUtils.createElement(ownerDoc, "PGPKeyID", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            keyIdElem.appendChild(ownerDoc.createTextNode(Base64.encode(this.keyId)));
            pdElem.appendChild(keyIdElem);
        }
        if (this.keyPacket != null) {
            Element keyPktElem = DOMUtils.createElement(ownerDoc, "PGPKeyPacket", "http://www.w3.org/2000/09/xmldsig#", dsPrefix);
            keyPktElem.appendChild(ownerDoc.createTextNode(Base64.encode(this.keyPacket)));
            pdElem.appendChild(keyPktElem);
        }
        for (XMLStructure extElem : this.externalElements) {
            DOMUtils.appendChild(pdElem, ((javax.xml.crypto.dom.DOMStructure)extElem).getNode());
        }
        parent.appendChild(pdElem);
    }

    private void checkKeyPacket(byte[] keyPacket) {
        if (keyPacket.length < 3) {
            throw new IllegalArgumentException("keypacket must be at least 3 bytes long");
        }
        byte tag = keyPacket[0];
        if ((tag & 0x80) != 128) {
            throw new IllegalArgumentException("keypacket tag is invalid: bit 7 is not set");
        }
        if ((tag & 0x40) != 64) {
            throw new IllegalArgumentException("old keypacket tag format is unsupported");
        }
        if ((tag & 6) != 6 && (tag & 0xE) != 14 && (tag & 5) != 5 && (tag & 7) != 7) {
            throw new IllegalArgumentException("keypacket tag is invalid: must be 6, 14, 5, or 7");
        }
    }
}

