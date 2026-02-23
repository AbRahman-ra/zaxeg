/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.gazt.einvoicing.signing.service.impl;

import com.gazt.einvoicing.digitalsignature.service.DigitalSignatureService;
import com.gazt.einvoicing.digitalsignature.service.impl.DigitalSignatureServiceImpl;
import com.gazt.einvoicing.digitalsignature.service.model.DigitalSignature;
import com.gazt.einvoicing.hashing.generation.service.HashingGenerationService;
import com.gazt.einvoicing.hashing.generation.service.impl.HashingGenerationServiceImpl;
import com.gazt.einvoicing.qr.generation.service.QRCodeGeneratorService;
import com.gazt.einvoicing.qr.generation.service.impl.QRCodeGeneratorServiceImpl;
import com.gazt.einvoicing.signing.service.SigningService;
import com.gazt.einvoicing.signing.service.model.InvoiceSigningResult;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

public class SigningServiceImpl
implements SigningService {
    private static final Logger LOGGER = Logger.getLogger(SigningServiceImpl.class.getName());
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private QRCodeGeneratorService qrCodeGeneratorService = new QRCodeGeneratorServiceImpl();
    private DigitalSignatureService digitalSignatureService = new DigitalSignatureServiceImpl();
    private HashingGenerationService hashingGenerationServiceImpl = new HashingGenerationServiceImpl();

    @Override
    public InvoiceSigningResult signDocument(String xmlDocument, InputStream privateKeyFile, InputStream certificatePublicKeyFile, String password) throws Exception {
        String privateKeyString = "-----BEGIN EC PRIVATE KEY-----\n" + new String(privateKeyFile.readAllBytes(), StandardCharsets.UTF_8).replaceAll("\n", "").replaceAll("\t", "") + "\n-----END EC PRIVATE KEY-----";
        InputStreamReader rdr = new InputStreamReader(new ByteArrayInputStream(privateKeyString.getBytes(StandardCharsets.UTF_8)));
        Object parsed = new PEMParser(rdr).readObject();
        KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)parsed);
        PrivateKey privateKey = pair.getPrivate();
        String certificateAsString = new String(certificatePublicKeyFile.readAllBytes());
        return this.signDocument(xmlDocument, privateKey, certificateAsString, password);
    }

    @Override
    public String generateInvoiceHash(String xmlDocument) throws Exception {
        return this.hashingGenerationServiceImpl.getInvoiceHash(xmlDocument);
    }

    @Override
    public InvoiceSigningResult signDocument(String xmlDocument, PrivateKey privateKey, String certificateAsString, String password) throws Exception {
        String invoiceHash = null;
        try {
            invoiceHash = this.hashingGenerationServiceImpl.getInvoiceHash(xmlDocument);
        } catch (Exception e) {
            throw new Exception("unable to generate hash for the provided invoice xml document - " + e.getMessage());
        }
        InvoiceSigningResult invoiceSigningResult = new InvoiceSigningResult();
        invoiceSigningResult.setInvoiceHash(invoiceHash);
        Security.addProvider(new BouncyCastleProvider());
        byte[] certificateBytes = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            certificateBytes = certificateAsString.getBytes(StandardCharsets.UTF_8);
            byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificateBytes));
        } catch (Exception e) {
            throw new Exception("unable to decode the provided invoice xml document");
        }
        byte[] certificateBytesCopy = Arrays.copyOf(certificateBytes, certificateBytes.length);
        String certificateCopy = new String(certificateBytesCopy);
        CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)certificatefactory.generateCertificate(byteArrayInputStream);
        DigitalSignature digitalSignature = null;
        try {
            digitalSignature = this.digitalSignatureService.getDigitalSignature(xmlDocument, privateKey, invoiceHash);
        } catch (Exception e) {
            throw new Exception("unable to sign the provided invoice xml document - " + e.getMessage());
        }
        xmlDocument = this.transformXML(xmlDocument);
        Document document = this.getXmlDocument(xmlDocument);
        Map<String, String> nameSpacesMap = this.getNameSpacesMap();
        String qrCode = this.getNodeXmlValue(document, nameSpacesMap, "/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject");
        invoiceSigningResult.setIncludesQRCodeAlready(StringUtils.isNotBlank(qrCode));
        String certificateHashing = this.encodeBase64(this.bytesToHex(this.hashStringToBytes(certificateAsString.getBytes(StandardCharsets.UTF_8))).getBytes(StandardCharsets.UTF_8));
        String signedPropertiesHashing = this.populateSignedSignatureProperties(document, nameSpacesMap, certificateHashing, this.getCurrentTimestamp(), certificate.getIssuerDN().getName(), certificate.getSerialNumber().toString());
        this.populateUBLExtensions(document, nameSpacesMap, digitalSignature.getDigitalSignature(), signedPropertiesHashing, this.encodeBase64(digitalSignature.getXmlHashing()), certificateCopy);
        try {
            qrCode = this.populateQRCode(document, nameSpacesMap, certificate.getPublicKey().getEncoded(), digitalSignature.getDigitalSignature(), invoiceHash);
        } catch (Exception e) {
            throw new Exception("unable to generate qr code for the provided invoice xml document - " + e.getMessage());
        }
        invoiceSigningResult.setQrCode(qrCode);
        invoiceSigningResult.setSingedXML(document.asXML());
        return invoiceSigningResult;
    }

    private String transformXML(String xmlDocument) throws IOException, TransformerException {
        xmlDocument = this.transformXml(xmlDocument, "removeElements.xsl");
        xmlDocument = this.transformXml(xmlDocument, "addUBLElement.xsl");
        xmlDocument = xmlDocument.replace("UBL-TO-BE-REPLACED", this.getElementFromFile("ubl.xml"));
        xmlDocument = this.transformXml(xmlDocument, "addQRElement.xsl");
        xmlDocument = xmlDocument.replace("QR-TO-BE-REPLACED", this.getElementFromFile("qr.xml"));
        xmlDocument = this.transformXml(xmlDocument, "addSignatureElement.xsl");
        xmlDocument = xmlDocument.replace("SIGN-TO-BE-REPLACED", this.getElementFromFile("signature.xml"));
        return xmlDocument;
    }

    private Map<String, String> getNameSpacesMap() {
        HashMap<String, String> nameSpaces = new HashMap<String, String>();
        nameSpaces.put("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
        nameSpaces.put("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
        nameSpaces.put("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
        nameSpaces.put("sig", "urn:oasis:names:specification:ubl:schema:xsd:CommonSignatureComponents-2");
        nameSpaces.put("sac", "urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2");
        nameSpaces.put("sbc", "urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2");
        nameSpaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
        nameSpaces.put("xades", "http://uri.etsi.org/01903/v1.3.2#");
        return nameSpaces;
    }

    private byte[] hashStringToBytes(byte[] toBeHashed) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(toBeHashed);
        return hash;
    }

    String encodeBase64(byte[] stringTobBeEncoded) {
        return Base64.getEncoder().encodeToString(stringTobBeEncoded);
    }

    private String populateQRCode(Document document, Map<String, String> nameSpacesMap, byte[] publicKey, String signature, String hashedXml) throws Exception {
        String sellerName = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName");
        String vatRegistrationNumber = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme/cbc:CompanyID");
        String invoiceTotal = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:LegalMonetaryTotal/cbc:TaxInclusiveAmount");
        String vatTotal = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cac:TaxTotal/cbc:TaxAmount");
        String issueDate = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:IssueDate");
        String issueTime = this.getNodeXmlTextValue(document, nameSpacesMap, "/Invoice/cbc:IssueTime");
        String issueDateTime = issueDate + " " + issueTime;
        LocalDateTime localDateTime = LocalDateTime.parse(issueDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timeStamp = this.dateTimeFormatter.format(localDateTime);
        String qrCode = this.qrCodeGeneratorService.generateQrCode(sellerName, vatRegistrationNumber, timeStamp, invoiceTotal, vatTotal, hashedXml, publicKey, signature);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/cac:AdditionalDocumentReference[cbc:ID='QR']/cac:Attachment/cbc:EmbeddedDocumentBinaryObject", qrCode);
        return qrCode;
    }

    private void populateUBLExtensions(Document document, Map<String, String> nameSpacesMap, String digitalSignature, String signedPropertiesHashing, String xmlHashing, String certificate) {
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:SignatureValue", digitalSignature);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate", certificate);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:SignedInfo/ds:Reference[@URI='#xadesSignedProperties']/ds:DigestValue", signedPropertiesHashing);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:SignedInfo/ds:Reference[@Id='invoiceSignedData']/ds:DigestValue", xmlHashing);
    }

    private String populateSignedSignatureProperties(Document document, Map<String, String> nameSpacesMap, String publicKeyHashing, String signatureTimestamp, String x509IssuerName, String serialNumber) throws NoSuchAlgorithmException {
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:CertDigest/ds:DigestValue", publicKeyHashing);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedSignatureProperties/xades:SigningTime", signatureTimestamp);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:IssuerSerial/ds:X509IssuerName", x509IssuerName);
        this.populateXmlAttributeValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties/xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:IssuerSerial/ds:X509SerialNumber", serialNumber);
        String signedSignatureElement = this.getNodeXmlValue(document, nameSpacesMap, "/Invoice/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/sig:UBLDocumentSignatures/sac:SignatureInformation/ds:Signature/ds:Object/xades:QualifyingProperties/xades:SignedProperties");
        return this.encodeBase64(this.bytesToHex(this.hashStringToBytes(signedSignatureElement.getBytes(StandardCharsets.UTF_8))).getBytes(StandardCharsets.UTF_8));
    }

    private Document populateXmlAttributeValue(Document document, Map<String, String> nameSpaces, String attributeXpath, String newValue) {
        XPath xpath = DocumentHelper.createXPath(attributeXpath);
        xpath.setNamespaceURIs(nameSpaces);
        List nodes = xpath.selectNodes(document);
        IntStream.range(0, nodes.size()).mapToObj(i -> (Element)nodes.get(i)).forEach(element -> element.setText(newValue));
        return document;
    }

    private String getNodeXmlValue(Document document, Map<String, String> nameSpaces, String attributeXpath) {
        XPath xpath = DocumentHelper.createXPath(attributeXpath);
        xpath.setNamespaceURIs(nameSpaces);
        Node node = xpath.selectSingleNode(document);
        if (node != null) {
            return node.asXML();
        }
        return null;
    }

    private String getNodeXmlTextValue(Document document, Map<String, String> nameSpaces, String attributeXpath) {
        XPath xpath = DocumentHelper.createXPath(attributeXpath);
        xpath.setNamespaceURIs(nameSpaces);
        return xpath.selectSingleNode(document).getText();
    }

    private String getCurrentTimestamp() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String signatureTimestamp = this.dateTimeFormatter.format(localDateTime);
        return signatureTimestamp;
    }

    Document getXmlDocument(String xmlDocument) throws SAXException, DocumentException {
        SAXReader xmlReader = new SAXReader();
        Document doc = xmlReader.read(new ByteArrayInputStream(xmlDocument.getBytes(StandardCharsets.UTF_8)));
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        return doc;
    }

    private String transformXml(String xmlDocument, String fileName) throws IOException, TransformerException {
        Transformer transformer = this.getXsltTransformer(fileName);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult xmlOutput = new StreamResult(bos);
        transformer.transform(new StreamSource(new StringReader(xmlDocument)), xmlOutput);
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(0xFF & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private Transformer getXsltTransformer(String fileName) throws TransformerConfigurationException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(new ClassPathResource("xslt/" + fileName).getInputStream()));
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "no");
        return transformer;
    }

    private String getElementFromFile(String fileName) throws IOException {
        String text = new BufferedReader(new InputStreamReader(new ClassPathResource("xml/" + fileName).getInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        return text;
    }
}

