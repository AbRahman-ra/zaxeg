/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.implementations;

import java.io.OutputStream;
import org.apache.xml.security.signature.NodeFilter;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TransformEnvelopedSignature
extends TransformSpi {
    public static final String implementedTransformURI = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";

    protected String engineGetURI() {
        return implementedTransformURI;
    }

    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream os, Transform transformObject) throws TransformationException {
        Node signatureElement = transformObject.getElement();
        signatureElement = TransformEnvelopedSignature.searchSignatureElement(signatureElement);
        input.setExcludeNode(signatureElement);
        input.addNodeFilter(new EnvelopedNodeFilter(signatureElement));
        return input;
    }

    private static Node searchSignatureElement(Node signatureElement) throws TransformationException {
        boolean found = false;
        while (signatureElement != null && signatureElement.getNodeType() != 9) {
            Element el = (Element)signatureElement;
            if (el.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#") && el.getLocalName().equals("Signature")) {
                found = true;
                break;
            }
            signatureElement = signatureElement.getParentNode();
        }
        if (!found) {
            throw new TransformationException("transform.envelopedSignatureTransformNotInSignatureElement");
        }
        return signatureElement;
    }

    static class EnvelopedNodeFilter
    implements NodeFilter {
        Node exclude;

        EnvelopedNodeFilter(Node n) {
            this.exclude = n;
        }

        public int isNodeIncludeDO(Node n, int level) {
            if (n == this.exclude) {
                return -1;
            }
            return 1;
        }

        public int isNodeInclude(Node n) {
            if (n == this.exclude || XMLUtils.isDescendantOrSelf(this.exclude, n)) {
                return -1;
            }
            return 1;
        }
    }
}

