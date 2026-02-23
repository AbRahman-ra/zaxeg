/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.zatca.sdk.dto.ApplicationPropertyDto;
import com.zatca.sdk.dto.CsrGeneratorDto;
import com.zatca.sdk.service.GeneratorTemplate;
import com.zatca.sdk.util.ECDSAUtil;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.microsoft.MicrosoftObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;

public class CsrGenerationService
extends GeneratorTemplate {
    private static final Logger LOG = Logger.getLogger(CsrGenerationService.class);
    private CsrGeneratorDto csrGeneratorDto;

    private Boolean generateCsrFile(ApplicationPropertyDto property, CsrGeneratorDto csrGeneratorDto) {
        try {
            LOG.debug("generate csr file ");
            String csr = "";
            csr = property.isOutputPemFormat() ? csrGeneratorDto.getCsrPemFormat() : csrGeneratorDto.getCsr();
            this.generateFile(property.getCsrFileName(), csr);
            return true;
        } catch (Exception e) {
            LOG.error("failed to generate csr [unable to generate csr file] ");
            return false;
        }
    }

    private boolean validateCsrConfigInputFile(CsrGeneratorDto csrGeneratorDto) {
        LOG.debug("validate csr configuration file ");
        if (csrGeneratorDto.getCommonName() == null || csrGeneratorDto.getCommonName().trim().isEmpty()) {
            LOG.error("common name is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getSerialNumber() == null || csrGeneratorDto.getSerialNumber().trim().isEmpty()) {
            LOG.error("serial number is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getOrganizationIdentifier() == null || csrGeneratorDto.getOrganizationIdentifier().trim().isEmpty()) {
            LOG.error("organization identifier is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getOrganizationIdentifier().length() != 15) {
            LOG.error("invalid organization identifier, please provide a valid 15 digit of your vat number");
            return false;
        }
        if (csrGeneratorDto.getOrganizationUnitName() == null || csrGeneratorDto.getOrganizationUnitName().trim().isEmpty()) {
            LOG.error("organization unit name is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getOrganizationUnitName().length() != 10) {
            LOG.error("invalid organization unit name, please provide a valid 10 digit of your tin number");
            return false;
        }
        if (csrGeneratorDto.getOrganizationName() == null || csrGeneratorDto.getOrganizationName().trim().isEmpty()) {
            LOG.error("organization name is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getOrganizationName().length() != 10) {
            LOG.error("invalid organization name, please provide a valid 10 digit of your tin number");
            return false;
        }
        if (csrGeneratorDto.getCountryName() == null || csrGeneratorDto.getCountryName().trim().isEmpty()) {
            LOG.error("country code name is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getCountryName().length() > 3 || csrGeneratorDto.getCountryName().length() < 2) {
            LOG.error("invalid country code name, please provide a valid country code name");
            return false;
        }
        if (csrGeneratorDto.getInvoiceType() == null || csrGeneratorDto.getInvoiceType().trim().isEmpty()) {
            LOG.error("invoice type is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getInvoiceType().length() != 4) {
            LOG.error("invalid invoice type, please provide a valid invoice type");
            return false;
        }
        if (!Pattern.matches("^[0-1]{4}$", csrGeneratorDto.getInvoiceType())) {
            LOG.error("invalid invoice type, please provide a valid invoice type");
            return false;
        }
        if (csrGeneratorDto.getLocation() == null || csrGeneratorDto.getLocation().trim().isEmpty()) {
            LOG.error("location is mandatory field");
            return false;
        }
        if (csrGeneratorDto.getIndustry() == null || csrGeneratorDto.getIndustry().trim().isEmpty()) {
            LOG.error("industry is mandatory filed");
            return false;
        }
        return true;
    }

    private Boolean generatePrivateKeyFile(ApplicationPropertyDto property, CsrGeneratorDto csrGeneratorDto) {
        try {
            LOG.debug("generate private key file ");
            String privateKey = "";
            privateKey = property.isOutputPemFormat() ? csrGeneratorDto.getPrivateKeyPemFormat() : csrGeneratorDto.getPrivateKey();
            this.generateFile(property.getPrivateKeyFileName(), privateKey);
            return true;
        } catch (Exception e) {
            LOG.error("failed to generate csr [unable to generate private key file] ");
            return false;
        }
    }

    private CsrGeneratorDto mappingCsrInputData(Properties csrInputDataProperty) {
        CsrGeneratorDto csrInputDto = new CsrGeneratorDto(csrInputDataProperty.get("csr.common.name"), csrInputDataProperty.get("csr.serial.number"), csrInputDataProperty.get("csr.organization.identifier"), csrInputDataProperty.get("csr.organization.unit.name"), csrInputDataProperty.get("csr.organization.name"), csrInputDataProperty.get("csr.country.name"), csrInputDataProperty.get("csr.invoice.type"), csrInputDataProperty.get("csr.location.address"), csrInputDataProperty.get("csr.industry.business.category"));
        return csrInputDto;
    }

    private Properties loadCsrConfigFile(String csrConfigFileName) {
        LOG.debug("load csr configuration file ");
        FileInputStream input = null;
        try {
            input = new FileInputStream(csrConfigFileName);
        } catch (FileNotFoundException e) {
            LOG.error("failed to generate csr [csr config file is not found] ");
            return null;
        }
        Properties csrInputDataProperty = new Properties();
        try {
            csrInputDataProperty.load(input);
        } catch (IOException e) {
            LOG.error("failed to generate csr [unable to load csr config data] ");
            return null;
        }
        return csrInputDataProperty;
    }

    private String loadKey(String fileName) {
        LOG.debug("load key");
        try {
            return new String(IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("failed to generate csr [unsupported encoding private key] ");
            return null;
        } catch (IOException e) {
            LOG.error("failed to generate csr [unable to load private key] ");
            return null;
        }
    }

    private String transform(String type, byte[] certificateRequest) {
        try {
            LOG.debug("transform certificate into string");
            PemObject pemObject = new PemObject(type, certificateRequest);
            StringWriter stringWriter = new StringWriter();
            PEMWriter pemWriter = new PEMWriter(stringWriter);
            pemWriter.writeObject(pemObject);
            pemWriter.close();
            stringWriter.close();
            return stringWriter.toString();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    private Boolean generate(CsrGeneratorDto csrGeneratorDto, boolean isNonPrd) {
        LOG.debug("generate csr");
        KeyPair pair = null;
        try {
            pair = ECDSAUtil.getKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();
        X500NameBuilder subject = new X500NameBuilder();
        subject.addRDN(BCStyle.C, csrGeneratorDto.getCountryName());
        subject.addRDN(BCStyle.OU, csrGeneratorDto.getOrganizationUnitName());
        subject.addRDN(BCStyle.O, csrGeneratorDto.getOrganizationName());
        subject.addRDN(BCStyle.CN, csrGeneratorDto.getCommonName());
        X500Name x500 = subject.build();
        X500NameBuilder x500NameBuilderOtherAttributes = new X500NameBuilder();
        x500NameBuilderOtherAttributes.addRDN(RFC4519Style.sn, csrGeneratorDto.getSerialNumber());
        x500NameBuilderOtherAttributes.addRDN(BCStyle.UID, csrGeneratorDto.getOrganizationIdentifier());
        x500NameBuilderOtherAttributes.addRDN(RFC4519Style.title, csrGeneratorDto.getInvoiceType());
        x500NameBuilderOtherAttributes.addRDN(RFC4519Style.registeredAddress, csrGeneratorDto.getLocation());
        x500NameBuilderOtherAttributes.addRDN(RFC4519Style.businessCategory, csrGeneratorDto.getIndustry());
        X500Name x500OtherAttributes = x500NameBuilderOtherAttributes.build();
        Extension subjectAltName = null;
        try {
            String certificateTemplateName = "";
            certificateTemplateName = !isNonPrd ? "ZATCA-Code-Signing" : "TSTZATCA-Code-Signing";
            subjectAltName = new Extension(MicrosoftObjectIdentifiers.microsoftCertTemplateV1, false, (ASN1OctetString)new DEROctetString(new DisplayText(2, certificateTemplateName)));
        } catch (IOException e) {
            LOG.error("failed to generate csr [unable to encode of an ASN.1 object] ");
            return false;
        }
        GeneralName[] generalNamesArray = new GeneralName[]{new GeneralName(x500OtherAttributes)};
        GeneralNames generalNames = new GeneralNames(generalNamesArray);
        ContentSigner signGen = null;
        try {
            signGen = new JcaContentSignerBuilder("SHA256WITHECDSA").build(privateKey);
        } catch (OperatorCreationException e) {
            LOG.error("failed to generate csr [unable to build sign generator] ");
            return false;
        }
        JcaPKCS10CertificationRequestBuilder certificateBuilder = new JcaPKCS10CertificationRequestBuilder(x500, publicKey);
        try {
            certificateBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, new Extensions(new Extension[]{subjectAltName, Extension.create(Extension.subjectAlternativeName, false, generalNames)})).build(signGen);
        } catch (IOException e2) {
            LOG.error("failed to generate csr [the value of subject template name cannot be encoded into bytes] ");
            return false;
        }
        PKCS10CertificationRequest certRequest = certificateBuilder.build(signGen);
        String certificateStr = null;
        try {
            certificateStr = this.transform("CERTIFICATE REQUEST", certRequest.getEncoded());
        } catch (IOException e1) {
            LOG.error("failed to generate csr [the value of csr cannot be encoded into bytes] ");
            return false;
        }
        if (certificateStr == null) {
            return false;
        }
        try {
            csrGeneratorDto.setCsr(new String(Base64.getEncoder().encode(certificateStr.getBytes()), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("failed to generate csr [charset is not supported] ");
            return false;
        }
        String privateKeyPEM = null;
        try {
            privateKeyPEM = this.transform("EC PRIVATE KEY", privateKey.getEncoded());
        } catch (Exception e1) {
            LOG.error("failed to generate private key [the value of private key cannot be encoded into bytes] ");
            return false;
        }
        if (privateKeyPEM == null) {
            return false;
        }
        String key = privateKeyPEM.replace("-----BEGIN EC PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "").replace("-----END EC PRIVATE KEY-----", "");
        csrGeneratorDto.setCsrPemFormat(certificateStr);
        csrGeneratorDto.setPrivateKey(key);
        csrGeneratorDto.setPrivateKeyPemFormat(privateKeyPEM);
        return true;
    }

    @Override
    public boolean loadInput() {
        Properties csrInputDataProperty = this.loadCsrConfigFile(this.property.getCsrConfigFileName());
        if (csrInputDataProperty == null) {
            return false;
        }
        this.csrGeneratorDto = this.mappingCsrInputData(csrInputDataProperty);
        return this.csrGeneratorDto != null;
    }

    @Override
    public boolean validateInput() {
        Boolean returnResult = this.validateCsrConfigInputFile(this.csrGeneratorDto);
        return returnResult != false;
    }

    @Override
    public boolean process() {
        boolean returnResult = this.generate(this.csrGeneratorDto, this.property.isNonPrdServer());
        return returnResult;
    }

    @Override
    public boolean generateOutput() {
        boolean returnResult = this.generatePrivateKeyFile(this.property, this.csrGeneratorDto);
        if (!returnResult) {
            return false;
        }
        returnResult = this.generateCsrFile(this.property, this.csrGeneratorDto);
        if (!returnResult) {
            return false;
        }
        LOG.info("csr and private key have been generated successfully.");
        return true;
    }
}

