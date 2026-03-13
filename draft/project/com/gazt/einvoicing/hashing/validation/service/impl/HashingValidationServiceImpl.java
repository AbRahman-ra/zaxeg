/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.hashing.validation.service.impl;

import com.gazt.einvoicing.hashing.validation.service.HashingValidationService;
import com.gazt.einvoicing.hashing.validation.service.model.HashValidationResult;
import com.gazt.einvoicing.hashing.validation.service.model.InvoiceType;
import com.gazt.einvoicing.hashing.validation.service.model.PIHValidationResult;
import com.gazt.einvoicing.hashing.validation.service.model.ValidationPIHItem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class HashingValidationServiceImpl
implements HashingValidationService {
    private static final Logger LOG = Logger.getLogger(HashingValidationServiceImpl.class.getName());

    @Override
    public HashValidationResult validateEInvoiceHash(String xmlDocument) throws ParserConfigurationException, TransformerConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(xmlDocument.getBytes(StandardCharsets.UTF_8)));
        XPath xpath = this.getXPath();
        Transformer transformer = this.getTransformer();
        HashValidationResult hashValidationResult = new HashValidationResult();
        this.validateXMLHashing(document, xpath, transformer, hashValidationResult);
        return hashValidationResult;
    }

    private void validateXMLHashing(Document document, XPath xpath, Transformer transformer, HashValidationResult hashValidationResult) {
        try {
            XPathExpression hashElementExpr = xpath.compile("//*[local-name()='Invoice']//*[local-name()='UBLExtensions']//*[local-name()='UBLExtension']//*[local-name()='ExtensionContent']//*[local-name()='SignatureInformation']//*[local-name()='Signature']//*[local-name()='SignedInfo']//*[local-name()='Reference'][@Id='invoiceSignedData']//*[local-name()='DigestValue']");
            Node hashXMLNode = (Node)hashElementExpr.evaluate(document, XPathConstants.NODE);
            String xmlHashingFromXML = hashXMLNode.getTextContent();
            hashValidationResult.setXmlInvoiceHash(xmlHashingFromXML);
            XPathExpression invoiceTypeCodeElementExpr = xpath.compile("//*[local-name()='Invoice']//*[local-name()='InvoiceTypeCode']");
            Node invoiceTypeCodeXMLNode = (Node)invoiceTypeCodeElementExpr.evaluate(document, XPathConstants.NODE);
            String invoiceTypeCodeName = invoiceTypeCodeXMLNode.getAttributes().getNamedItem("name").getNodeValue();
            hashValidationResult.setInvoiceType(InvoiceType.byCode(invoiceTypeCodeName.substring(0, 2)));
            String xmlDocHashing = this.getXMLHashing(document, xpath, transformer);
            hashValidationResult.setValid(xmlDocHashing.equals(xmlHashingFromXML));
        } catch (Exception e) {
            LOG.fine("Error : " + e.getMessage());
            hashValidationResult.setValid(false);
        }
    }

    @Override
    public PIHValidationResult validateEInvoiceHash(List<String> xmlDocument) throws TransformerException, ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        PIHValidationResult pihValidationResult = new PIHValidationResult();
        ArrayList<ValidationPIHItem> validationPIHItemList = new ArrayList<ValidationPIHItem>();
        int index = 0;
        for (String xmlDoc : xmlDocument) {
            Document document = this.getDocument(xmlDoc);
            XPath xpath = this.getXPath();
            Transformer transformer = this.getTransformer();
            String pihXmlDoc = this.getPIH(document, xpath);
            String xmlDocHashing = this.getXMLHashing(document, xpath, transformer);
            ValidationPIHItem validationPIHItem = new ValidationPIHItem(++index, xmlDocHashing, pihXmlDoc);
            validationPIHItemList.add(validationPIHItem);
        }
        for (ValidationPIHItem validationPIHErrorOuter : validationPIHItemList) {
            for (ValidationPIHItem validationPIHErrorInner : validationPIHItemList) {
                if (!validationPIHErrorOuter.getXmHashing().equals(validationPIHErrorInner.getXmlPIH())) continue;
                validationPIHErrorInner.setValidPIH(true);
            }
        }
        long count = validationPIHItemList.stream().filter(ValidationPIHItem::isValidPIH).count();
        if (count > 0L && count == (long)(validationPIHItemList.size() - 1)) {
            pihValidationResult.setValid(true);
            return pihValidationResult;
        }
        pihValidationResult.setValid(false);
        return pihValidationResult;
    }

    private String getXMLHashing(Document document, XPath xpath, Transformer transformer) throws TransformerException {
        ArrayList<String> pathsToBeRemoved = new ArrayList<String>();
        pathsToBeRemoved.add("//*[local-name()='Invoice']//*[local-name()='UBLExtensions']");
        pathsToBeRemoved.add("//*[local-name()='Invoice']//*[local-name()='Signature']");
        pathsToBeRemoved.add("//*[local-name()='Invoice']//*[local-name()='AdditionalDocumentReference']//*[text()= 'QR']/parent::*");
        pathsToBeRemoved.forEach(path -> {
            try {
                XPathExpression expr = xpath.compile((String)path);
                Node tobeDeleted = (Node)expr.evaluate(document, XPathConstants.NODE);
                if (tobeDeleted != null) {
                    tobeDeleted.getParentNode().removeChild(tobeDeleted);
                }
            } catch (XPathExpressionException e) {
                LOG.fine("Error : " + e.getMessage());
            }
        });
        DOMSource source = new DOMSource(document);
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        transformer.transform(source, xmlOutput);
        return Base64.getEncoder().encodeToString(this.hashStringToBytes(xmlOutput.getWriter().toString().replaceAll("[\\n\\t ]", "").replaceAll("\r", "")));
    }

    private String getPIH(Document document, XPath xpath) throws XPathExpressionException {
        XPathExpression hashElementExpr = xpath.compile("//*[local-name()='Invoice']//*[local-name()='AdditionalDocumentReference']//*[local-name()='ID'][text()= 'PIH']//parent::*//*[local-name()='Attachment']//*[local-name()='EmbeddedDocumentBinaryObject']");
        Node hashXMLNode = (Node)hashElementExpr.evaluate(document, XPathConstants.NODE);
        if (hashXMLNode != null) {
            return hashXMLNode.getTextContent();
        }
        return null;
    }

    private Document getDocument(String xmlDocument) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(xmlDocument.getBytes(StandardCharsets.UTF_8)));
        return document;
    }

    private XPath getXPath() {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        return xpath;
    }

    private Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("omit-xml-declaration", "yes");
        return transformer;
    }

    private byte[] hashStringToBytes(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return hash;
    }
}

