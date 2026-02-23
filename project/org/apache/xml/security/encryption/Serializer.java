/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface Serializer {
    public void setCanonicalizer(Canonicalizer var1);

    @Deprecated
    public String serialize(Element var1) throws Exception;

    public byte[] serializeToByteArray(Element var1) throws Exception;

    @Deprecated
    public String serialize(NodeList var1) throws Exception;

    public byte[] serializeToByteArray(NodeList var1) throws Exception;

    @Deprecated
    public String canonSerialize(Node var1) throws Exception;

    public byte[] canonSerializeToByteArray(Node var1) throws Exception;

    @Deprecated
    public Node deserialize(String var1, Node var2) throws XMLEncryptionException;

    public Node deserialize(byte[] var1, Node var2) throws XMLEncryptionException;
}

