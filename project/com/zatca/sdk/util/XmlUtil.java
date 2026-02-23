/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlUtil {
    private static final Logger LOG = Logger.getLogger(XmlUtil.class);

    public static String load(String fileName) {
        try {
            return IOUtils.toString(new InputStreamReader(XmlUtil.class.getClassLoader().getResourceAsStream(fileName)));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static String load(InputStream stream) {
        try {
            return IOUtils.toString(new InputStreamReader(stream));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static String transform(Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static Document transform(String document) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newDocumentBuilder().parse(new InputSource(new StringReader(document)));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static NodeList evaluateXpath(Document document, String xpathExpression) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(xpathExpression);
            return (NodeList)expr.evaluate(document.getDocumentElement(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}

