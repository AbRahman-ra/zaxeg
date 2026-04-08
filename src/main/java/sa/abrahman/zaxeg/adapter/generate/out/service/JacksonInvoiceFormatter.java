package sa.abrahman.zaxeg.adapter.generate.out.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import sa.abrahman.zaxeg.adapter.generate.out.dto.XmlInvoice;
import sa.abrahman.zaxeg.adapter.generate.out.exception.XmlGenerationException;
import sa.abrahman.zaxeg.adapter.generate.out.constant.ZatcaDefaultValues;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.aggregate.XmlAggregatePaymentMeans;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataAdditionalDocumentReference;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataBillingReference;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataDelivery;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataDocumentReference;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataDocumentReferenceAttachment;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataEmbeddedDocumentBinaryObject;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata.XmlMetadataInvoiceTypeCode;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesAccountingParty;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesCountry;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesParty;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesPartyIdentification;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesPartyLegalEntity;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesPartyTaxScheme;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesPostalAddress;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesSchemeId;
import sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties.XmlPartiesTaxScheme;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.party.Party;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.wrapper.InvoiceParties;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.wrapper.Metadata;
import sa.abrahman.zaxeg.core.generate.port.out.InvoiceFormatter;

@Component
public class JacksonInvoiceFormatter implements InvoiceFormatter {
    private final XmlMapper mapper;

    public JacksonInvoiceFormatter() {
        this.mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
    }

    @Override
    public String format(Invoice invoice) {
        try {
            XmlInvoice dto = toDto(invoice);
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new XmlGenerationException("Failed to serialize ZATCA XML", e);
        }
    }

    private XmlInvoice toDto(Invoice i) {
        Metadata meta = i.getMetadata();
        InvoiceParties parties = i.getParties();
        XmlInvoice.XmlInvoiceBuilder builder = XmlInvoice.builder();

        formatMetadata(meta, builder);
        formatParties(parties, builder);

        return builder.build();
    }

    private void formatMetadata(Metadata meta, XmlInvoice.XmlInvoiceBuilder builder) {
        String typeCodeValue = String.valueOf(meta.getInvoiceDocumentType().getCode()); // e.g., "388"
        String typeCodeName = meta.getInvoiceTypeTransactions().code();
        XmlMetadataInvoiceTypeCode typeCode = new XmlMetadataInvoiceTypeCode(typeCodeName, typeCodeValue);

        builder.id(meta.getInvoiceNumber()).uuid(meta.getInvoiceUuid().toString())
                .issueDate(meta.getInvoiceIssueDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .issueTime(meta.getInvoiceIssueTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .invoiceTypeCode(typeCode).documentCurrencyCode(meta.getInvoiceCurrency().getCurrencyCode())
                .taxCurrencyCode(meta.getTaxCurrency().getCurrencyCode()).notes(meta.getNotes());

        if (meta.getBillingReference() != null) {
            XmlMetadataBillingReference billingRef = new XmlMetadataBillingReference(
                    new XmlMetadataDocumentReference(meta.getBillingReference().getId()));
            builder.billingReference(billingRef);
        }

        if (meta.getPurchaseOrder() != null) {
            XmlMetadataDocumentReference purchaseOrder = new XmlMetadataDocumentReference(
                    meta.getPurchaseOrder().getId());
            builder.purchaseOrder(purchaseOrder);
        }

        if (meta.getContract() != null) {
            XmlMetadataDocumentReference contract = new XmlMetadataDocumentReference(meta.getContract().getId());
            builder.contract(contract);
        }

        List<XmlMetadataAdditionalDocumentReference> additionalRefs = new ArrayList<>();

        if (meta.getIcv() != null) {
            XmlMetadataAdditionalDocumentReference icv = XmlMetadataAdditionalDocumentReference.builder()
                    .id(ZatcaDefaultValues.ICV_DOCUMENT_REFERENCE_ID).uuid(meta.getIcv().toString()).build();
            additionalRefs.add(icv);
        }

        if (meta.getPih() != null && !meta.getPih().isBlank()) {
            XmlMetadataEmbeddedDocumentBinaryObject pihBo = XmlMetadataEmbeddedDocumentBinaryObject.builder()
                    .mimeCode(MediaType.TEXT_PLAIN_VALUE).value(meta.getPih()).build();

            XmlMetadataDocumentReferenceAttachment pihAttach = XmlMetadataDocumentReferenceAttachment.builder()
                    .embeddedDocumentBinaryObject(pihBo).build();

            XmlMetadataAdditionalDocumentReference pih = XmlMetadataAdditionalDocumentReference.builder()
                    .id(ZatcaDefaultValues.PIH_DOCUMENT_REFERENCE_ID).attachment(pihAttach).build();

            additionalRefs.add(pih);
        }

        if (meta.getQr() != null && !meta.getQr().isBlank()) {
            XmlMetadataEmbeddedDocumentBinaryObject qrBo = XmlMetadataEmbeddedDocumentBinaryObject.builder()
                    .mimeCode(MediaType.TEXT_PLAIN_VALUE).value(meta.getQr()).build();

            XmlMetadataDocumentReferenceAttachment qrAttach = XmlMetadataDocumentReferenceAttachment.builder()
                    .embeddedDocumentBinaryObject(qrBo).build();

            XmlMetadataAdditionalDocumentReference qr = XmlMetadataAdditionalDocumentReference.builder()
                    .id(ZatcaDefaultValues.QR_DOCUMENT_REFERENCE_ID).attachment(qrAttach).build();

            additionalRefs.add(qr);
        }

        if (meta.getSupplyDate() != null) {
            String supplyEndDate = meta.getSupplyEndDate() != null
                    ? meta.getSupplyEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : null;
            builder.delivery(new XmlMetadataDelivery(meta.getSupplyDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    supplyEndDate));
        }

        if (meta.getCreditOrDebitNoteIssuanceReasons() != null
                && !meta.getCreditOrDebitNoteIssuanceReasons().isEmpty()) {
            XmlAggregatePaymentMeans means = XmlAggregatePaymentMeans.builder()
                    .creditOrDebitNoteIssuanceReasons(meta.getCreditOrDebitNoteIssuanceReasons()).build();
            builder.paymentMeans(means);
        }

        builder.additionalDocumentReferences(additionalRefs);
    }

    private void formatParties(InvoiceParties parties, XmlInvoice.XmlInvoiceBuilder builder) {
        builder.accountingSupplierParty(mapParty(parties.getSeller()));

        if (parties.getBuyer() != null) {
            builder.accountingCustomerParty(mapParty(parties.getBuyer()));
        }
    }

    private XmlPartiesAccountingParty mapParty(Party domainParty) {
        XmlPartiesParty.XmlPartiesPartyBuilder partyBuilder = XmlPartiesParty.builder();

        // 1. Legal Entity (Name)
        partyBuilder.partyLegalEntity(new XmlPartiesPartyLegalEntity(domainParty.getName()));

        // 2. Tax Scheme (VAT Number) - Maps to BT-31 (Seller) / BT-48 (Buyer)
        if (domainParty.getIdentification() != null) {
            String vatNumber = domainParty.getIdentification().getCompanyId();
            XmlPartiesTaxScheme vatScheme = new XmlPartiesTaxScheme(
                    domainParty.getIdentification().getTaxScheme().name());
            partyBuilder.partyTaxScheme(new XmlPartiesPartyTaxScheme(vatNumber, vatScheme));
        }

        // 3. Other Identifications (CRN, NAT, SAG, etc.) - Maps to BT-30 (Seller) / BT-47 (Buyer)
        if (domainParty.getOtherIds() != null && !domainParty.getOtherIds().isEmpty()) {
            List<XmlPartiesPartyIdentification> otherIds = domainParty.getOtherIds().stream()
                    .map(id -> new XmlPartiesPartyIdentification(
                            new XmlPartiesSchemeId(id.getSchemeId().name(), id.getValue())))
                    .toList();
            partyBuilder.partyIdentification(otherIds);
        }

        // 4. Postal Address - Specific ZATCA UBL mapping rules apply here
        if (domainParty.getAddress() != null) {
            var address = domainParty.getAddress();
            partyBuilder.postalAddress(XmlPartiesPostalAddress.builder().streetName(address.getStreet())
                    .additionalStreetName(address.getAdditionalStreet()).buildingNumber(address.getBuildingNumber())
                    .plotIdentification(address.getAdditionalNumber()) // ZATCA Rule: Additional Number maps to PlotIdentification
                    .citySubdivisionName(address.getDistrict()) // ZATCA Rule: District maps to CitySubdivisionName
                    .cityName(address.getCity()).postalZone(address.getPostalCode())
                    .countrySubentity(address.getProvinceOrState())
                    .country(new XmlPartiesCountry(address.getCountry().getCountry())).build());
        }

        return new XmlPartiesAccountingParty(partyBuilder.build());
    }
}
