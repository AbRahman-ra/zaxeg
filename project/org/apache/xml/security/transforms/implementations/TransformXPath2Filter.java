/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.transforms.implementations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.implementations.XPath2NodeFilter;
import org.apache.xml.security.transforms.params.XPath2FilterContainer;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.XPathAPI;
import org.apache.xml.security.utils.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TransformXPath2Filter
extends TransformSpi {
    public static final String implementedTransformURI = "http://www.w3.org/2002/06/xmldsig-filter2";

    protected String engineGetURI() {
        return implementedTransformURI;
    }

    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream os, Transform transformObject) throws TransformationException {
        try {
            ArrayList<NodeList> unionNodes = new ArrayList<NodeList>();
            ArrayList<NodeList> subtractNodes = new ArrayList<NodeList>();
            ArrayList<NodeList> intersectNodes = new ArrayList<NodeList>();
            Element[] xpathElements = XMLUtils.selectNodes(transformObject.getElement().getFirstChild(), implementedTransformURI, "XPath");
            if (xpathElements.length == 0) {
                Object[] exArgs = new Object[]{implementedTransformURI, "XPath"};
                throw new TransformationException("xml.WrongContent", exArgs);
            }
            Document inputDoc = null;
            inputDoc = input.getSubNode() != null ? XMLUtils.getOwnerDocument(input.getSubNode()) : XMLUtils.getOwnerDocument(input.getNodeSet());
            for (int i = 0; i < xpathElements.length; ++i) {
                Element xpathElement = xpathElements[i];
                XPath2FilterContainer xpathContainer = XPath2FilterContainer.newInstance(xpathElement, input.getSourceURI());
                String str = XMLUtils.getStrFromNode(xpathContainer.getXPathFilterTextNode());
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPathAPI xpathAPIInstance = xpathFactory.newXPathAPI();
                NodeList subtreeRoots = xpathAPIInstance.selectNodeList(inputDoc, xpathContainer.getXPathFilterTextNode(), str, xpathContainer.getElement());
                if (xpathContainer.isIntersect()) {
                    intersectNodes.add(subtreeRoots);
                    continue;
                }
                if (xpathContainer.isSubtract()) {
                    subtractNodes.add(subtreeRoots);
                    continue;
                }
                if (!xpathContainer.isUnion()) continue;
                unionNodes.add(subtreeRoots);
            }
            input.addNodeFilter(new XPath2NodeFilter(unionNodes, subtractNodes, intersectNodes));
            input.setNodeSet(true);
            return input;
        } catch (TransformerException ex) {
            throw new TransformationException("empty", ex);
        } catch (DOMException ex) {
            throw new TransformationException("empty", ex);
        } catch (CanonicalizationException ex) {
            throw new TransformationException("empty", ex);
        } catch (InvalidCanonicalizerException ex) {
            throw new TransformationException("empty", ex);
        } catch (XMLSecurityException ex) {
            throw new TransformationException("empty", ex);
        } catch (SAXException ex) {
            throw new TransformationException("empty", ex);
        } catch (IOException ex) {
            throw new TransformationException("empty", ex);
        } catch (ParserConfigurationException ex) {
            throw new TransformationException("empty", ex);
        }
    }
}

