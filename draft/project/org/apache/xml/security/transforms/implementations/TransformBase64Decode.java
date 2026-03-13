/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.implementations;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class TransformBase64Decode
extends TransformSpi {
    public static final String implementedTransformURI = "http://www.w3.org/2000/09/xmldsig#base64";

    protected String engineGetURI() {
        return implementedTransformURI;
    }

    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, Transform transformObject) throws IOException, CanonicalizationException, TransformationException {
        return this.enginePerformTransform(input, null, transformObject);
    }

    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream os, Transform transformObject) throws IOException, CanonicalizationException, TransformationException {
        try {
            if (input.isElement()) {
                Node el = input.getSubNode();
                if (input.getSubNode().getNodeType() == 3) {
                    el = el.getParentNode();
                }
                StringBuilder sb = new StringBuilder();
                this.traverseElement((Element)el, sb);
                if (os == null) {
                    byte[] decodedBytes = Base64.decode(sb.toString());
                    XMLSignatureInput output = new XMLSignatureInput(decodedBytes);
                    output.setSecureValidation(this.secureValidation);
                    return output;
                }
                Base64.decode(sb.toString(), os);
                XMLSignatureInput output = new XMLSignatureInput((byte[])null);
                output.setSecureValidation(this.secureValidation);
                output.setOutputStream(os);
                return output;
            }
            if (input.isOctetStream() || input.isNodeSet()) {
                if (os == null) {
                    byte[] base64Bytes = input.getBytes();
                    byte[] decodedBytes = Base64.decode(base64Bytes);
                    XMLSignatureInput output = new XMLSignatureInput(decodedBytes);
                    output.setSecureValidation(this.secureValidation);
                    return output;
                }
                if (input.isByteArray() || input.isNodeSet()) {
                    Base64.decode(input.getBytes(), os);
                } else {
                    Base64.decode(new BufferedInputStream(input.getOctetStreamReal()), os);
                }
                XMLSignatureInput output = new XMLSignatureInput((byte[])null);
                output.setSecureValidation(this.secureValidation);
                output.setOutputStream(os);
                return output;
            }
            try {
                Document doc = XMLUtils.createDocumentBuilder(false, this.secureValidation).parse(input.getOctetStream());
                Element rootNode = doc.getDocumentElement();
                StringBuilder sb = new StringBuilder();
                this.traverseElement(rootNode, sb);
                byte[] decodedBytes = Base64.decode(sb.toString());
                XMLSignatureInput output = new XMLSignatureInput(decodedBytes);
                output.setSecureValidation(this.secureValidation);
                return output;
            } catch (ParserConfigurationException e) {
                throw new TransformationException("c14n.Canonicalizer.Exception", e);
            } catch (SAXException e) {
                throw new TransformationException("SAX exception", e);
            }
        } catch (Base64DecodingException e) {
            throw new TransformationException("Base64Decoding", e);
        }
    }

    void traverseElement(Element node, StringBuilder sb) {
        block4: for (Node sibling = node.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
            switch (sibling.getNodeType()) {
                case 1: {
                    this.traverseElement((Element)sibling, sb);
                    continue block4;
                }
                case 3: {
                    sb.append(((Text)sibling).getData());
                }
            }
        }
    }
}

