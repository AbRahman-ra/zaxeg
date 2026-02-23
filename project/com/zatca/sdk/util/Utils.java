/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class);

    public static String getNodeContentXpth(File file, String tagPath) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(file);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(tagPath);
            return (String)expr.evaluate(doc, XPathConstants.STRING);
        } catch (Exception e) {
            LOG.error("Error : " + e.getMessage());
            return null;
        }
    }

    public static org.w3c.dom.Node getNodeXpath(File file, String tagPath) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(file);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(tagPath);
            return (org.w3c.dom.Node)expr.evaluate(doc, XPathConstants.NODE);
        } catch (Exception e) {
            LOG.error("Error : " + e.getMessage());
            return null;
        }
    }

    public static String getPihCode(File file) {
        String tagPath = "/Invoice/AdditionalDocumentReference[ID='PIH']/Attachment/EmbeddedDocumentBinaryObject";
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(file);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(tagPath);
            NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            LOG.debug("node list size:" + nl.getLength());
            return nl.item(0) == null ? "" : nl.item(0).getTextContent();
        } catch (Exception e) {
            LOG.error("Error : " + e.getMessage());
            return null;
        }
    }

    public static boolean isInvoiceSimplified(File invoice) {
        String invoiceType = Utils.getNodeXpath(invoice, "//*[local-name()='Invoice']//*[local-name()='InvoiceTypeCode']").getAttributes().getNamedItem("name").getNodeValue();
        return invoiceType.startsWith("02");
    }

    public static String getNodeXmlValue(String xmlDocument, String attributeXpath) throws DocumentException, SAXException {
        SAXReader xmlReader = new SAXReader();
        Document document = xmlReader.read(new ByteArrayInputStream(xmlDocument.getBytes(StandardCharsets.UTF_8)));
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        XPath xpath = DocumentHelper.createXPath(attributeXpath);
        HashMap<String, String> nameSpaces = new HashMap<String, String>();
        nameSpaces.put("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
        nameSpaces.put("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
        nameSpaces.put("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
        nameSpaces.put("sig", "urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2");
        nameSpaces.put("sac", "urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2");
        nameSpaces.put("sbc", "urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2");
        nameSpaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
        nameSpaces.put("xades", "http://uri.etsi.org/01903/v1.3.2#");
        xpath.setNamespaceURIs(nameSpaces);
        Node node = xpath.selectSingleNode(document);
        if (node != null) {
            return node.asXML();
        }
        return null;
    }
}

