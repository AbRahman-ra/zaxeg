package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.aggregate.XmlAggregatePaymentMeans;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.parties.XmlPartiesAccountingParty;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;
import sa.abrahman.zaxeg.infrastructure.out.factory.ZatcaDefaultValues;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = UblInvoiceElements.TAGS.INVOICE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
public class XmlInvoice {

    /** Namespaces (Required by ZATCA) */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.XMLNS.CAC)
    @Builder.Default
    private String xmlnsCac = UblInvoiceElements.NAMESPACES.CAC;

    /** Namespaces (Required by ZATCA) */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.XMLNS.CBC)
    @Builder.Default
    private String xmlnsCbc = UblInvoiceElements.NAMESPACES.CBC;

    /** Namespaces (Required by ZATCA) */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.XMLNS.EXT)
    @Builder.Default
    private String xmlnsExt = UblInvoiceElements.NAMESPACES.EXT;

    // ============================ METADATA ============================
    @Builder.Default
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.PROFILE_ID, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String profileId = ZatcaDefaultValues.DEFAULT_PROFILE_ID;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ID, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String id;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.UUID, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String uuid;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ISSUE_DATE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String issueDate;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ISSUE_TIME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String issueTime;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.INVOICE_TYPE_CODE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlMetadataInvoiceTypeCode invoiceTypeCode;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.NOTE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private List<String> notes;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.DOCUMENT_CURRENCY_CODE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String documentCurrencyCode;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.TAX_CURRENCY_CODE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String taxCurrencyCode;

    // --- Document References ---
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.BILLING_REFERENCE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlMetadataBillingReference billingReference;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ORDER_REFERENCE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlMetadataDocumentReference purchaseOrder;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.CONTRACT_DOCUMENT_REFERENCE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlMetadataDocumentReference contract;

    /**
     * Used for ICV, PIH, QR
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ADDITIONAL_DOCUMENT_REFERENCE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private List<XmlMetadataAdditionalDocumentReference> additionalDocumentReferences;

    // ============================ PARTIES ============================

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ACCOUNTING_SUPPLIER_PARTY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesAccountingParty accountingSupplierParty;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ACCOUNTING_CUSTOMER_PARTY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesAccountingParty accountingCustomerParty;

    // ============================ METADATA.DELIVERY_INFO ============================
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.DELIVERY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlMetadataDelivery delivery;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.PAYMENT_MEANS, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlAggregatePaymentMeans paymentMeans;
}
