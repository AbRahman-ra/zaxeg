/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.encryption.Serializer;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractSerializer
implements Serializer {
    protected Canonicalizer canon;
    protected boolean secureValidation;

    public void setCanonicalizer(Canonicalizer canon) {
        this.canon = canon;
    }

    @Deprecated
    public String serialize(Element element) throws Exception {
        return this.canonSerialize(element);
    }

    public byte[] serializeToByteArray(Element element) throws Exception {
        return this.canonSerializeToByteArray(element);
    }

    @Deprecated
    public String serialize(NodeList content) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.canon.setSecureValidation(this.secureValidation);
        this.canon.setWriter(baos);
        this.canon.notReset();
        for (int i = 0; i < content.getLength(); ++i) {
            this.canon.canonicalizeSubtree(content.item(i));
        }
        String ret = baos.toString("UTF-8");
        baos.reset();
        return ret;
    }

    public byte[] serializeToByteArray(NodeList content) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.canon.setSecureValidation(this.secureValidation);
        this.canon.setWriter(baos);
        this.canon.notReset();
        for (int i = 0; i < content.getLength(); ++i) {
            this.canon.canonicalizeSubtree(content.item(i));
        }
        return baos.toByteArray();
    }

    @Deprecated
    public String canonSerialize(Node node) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.canon.setSecureValidation(this.secureValidation);
        this.canon.setWriter(baos);
        this.canon.notReset();
        this.canon.canonicalizeSubtree(node);
        String ret = baos.toString("UTF-8");
        baos.reset();
        return ret;
    }

    public byte[] canonSerializeToByteArray(Node node) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.canon.setSecureValidation(this.secureValidation);
        this.canon.setWriter(baos);
        this.canon.notReset();
        this.canon.canonicalizeSubtree(node);
        return baos.toByteArray();
    }

    @Deprecated
    public abstract Node deserialize(String var1, Node var2) throws XMLEncryptionException;

    public abstract Node deserialize(byte[] var1, Node var2) throws XMLEncryptionException;

    protected static byte[] createContext(byte[] source, Node ctx) throws XMLEncryptionException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter((OutputStream)byteArrayOutputStream, "UTF-8");
            outputStreamWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummy");
            HashMap<String, String> storedNamespaces = new HashMap<String, String>();
            for (Node wk = ctx; wk != null; wk = wk.getParentNode()) {
                NamedNodeMap atts = wk.getAttributes();
                if (atts == null) continue;
                for (int i = 0; i < atts.getLength(); ++i) {
                    Node att = atts.item(i);
                    String nodeName = att.getNodeName();
                    if (!nodeName.equals("xmlns") && !nodeName.startsWith("xmlns:") || storedNamespaces.containsKey(att.getNodeName())) continue;
                    outputStreamWriter.write(" ");
                    outputStreamWriter.write(nodeName);
                    outputStreamWriter.write("=\"");
                    outputStreamWriter.write(att.getNodeValue());
                    outputStreamWriter.write("\"");
                    storedNamespaces.put(nodeName, att.getNodeValue());
                }
            }
            outputStreamWriter.write(">");
            outputStreamWriter.flush();
            byteArrayOutputStream.write(source);
            outputStreamWriter.write("</dummy>");
            outputStreamWriter.close();
            return byteArrayOutputStream.toByteArray();
        } catch (UnsupportedEncodingException e) {
            throw new XMLEncryptionException("empty", e);
        } catch (IOException e) {
            throw new XMLEncryptionException("empty", e);
        }
    }

    protected static String createContext(String source, Node ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummy");
        HashMap<String, String> storedNamespaces = new HashMap<String, String>();
        for (Node wk = ctx; wk != null; wk = wk.getParentNode()) {
            NamedNodeMap atts = wk.getAttributes();
            if (atts == null) continue;
            for (int i = 0; i < atts.getLength(); ++i) {
                Node att = atts.item(i);
                String nodeName = att.getNodeName();
                if (!nodeName.equals("xmlns") && !nodeName.startsWith("xmlns:") || storedNamespaces.containsKey(att.getNodeName())) continue;
                sb.append(" " + nodeName + "=\"" + att.getNodeValue() + "\"");
                storedNamespaces.put(nodeName, att.getNodeValue());
            }
        }
        sb.append(">" + source + "</dummy>");
        return sb.toString();
    }

    public boolean isSecureValidation() {
        return this.secureValidation;
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }
}

