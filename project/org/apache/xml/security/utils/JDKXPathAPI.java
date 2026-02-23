/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.utils;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.apache.xml.security.utils.DOMNamespaceContext;
import org.apache.xml.security.utils.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JDKXPathAPI
implements XPathAPI {
    private XPathFactory xpf;
    private String xpathStr;
    private XPathExpression xpathExpression;

    public NodeList selectNodeList(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        if (!str.equals(this.xpathStr) || this.xpathExpression == null) {
            if (this.xpf == null) {
                this.xpf = XPathFactory.newInstance();
                try {
                    this.xpf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
                } catch (XPathFactoryConfigurationException ex) {
                    throw new TransformerException("empty", ex);
                }
            }
            XPath xpath = this.xpf.newXPath();
            xpath.setNamespaceContext(new DOMNamespaceContext(namespaceNode));
            this.xpathStr = str;
            try {
                this.xpathExpression = xpath.compile(this.xpathStr);
            } catch (XPathExpressionException ex) {
                throw new TransformerException("empty", ex);
            }
        }
        try {
            return (NodeList)this.xpathExpression.evaluate(contextNode, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            throw new TransformerException("empty", ex);
        }
    }

    public boolean evaluate(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        if (!str.equals(this.xpathStr) || this.xpathExpression == null) {
            if (this.xpf == null) {
                this.xpf = XPathFactory.newInstance();
                try {
                    this.xpf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
                } catch (XPathFactoryConfigurationException ex) {
                    throw new TransformerException("empty", ex);
                }
            }
            XPath xpath = this.xpf.newXPath();
            xpath.setNamespaceContext(new DOMNamespaceContext(namespaceNode));
            this.xpathStr = str;
            try {
                this.xpathExpression = xpath.compile(this.xpathStr);
            } catch (XPathExpressionException ex) {
                throw new TransformerException("empty", ex);
            }
        }
        try {
            Boolean result = (Boolean)this.xpathExpression.evaluate(contextNode, XPathConstants.BOOLEAN);
            return result;
        } catch (XPathExpressionException ex) {
            throw new TransformerException("empty", ex);
        }
    }

    public void clear() {
        this.xpathStr = null;
        this.xpathExpression = null;
        this.xpf = null;
    }
}

