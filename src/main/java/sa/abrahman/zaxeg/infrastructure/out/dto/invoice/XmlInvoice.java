package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.*;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;
import sa.abrahman.zaxeg.infrastructure.out.factory.ZatcaDefaultValues;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = UblInvoiceElements.TAGS.INVOICE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
public class XmlInvoice {

    /** Namespaces (Required by ZATCA) */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.XMLNS.XMLNS_NS)
    @Builder.Default
    private String xmlns = UblInvoiceElements.NAMESPACES.ROOT;

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
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.PROFILE_ID)
    private String profileId = ZatcaDefaultValues.DEFAULT_PROFILE_ID;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ID)
    private String id;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.UUID)
    private String uuid;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ISSUE_DATE)
    private String issueDate;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ISSUE_TIME)
    private String issueTime;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.INVOICE_TYPE_CODE)
    private XmlInvoiceTypeCode invoiceTypeCode;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.NOTE)
    private List<String> notes;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.DOCUMENT_CURRENCY_CODE)
    private String documentCurrencyCode;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.TAX_CURRENCY_CODE)
    private String taxCurrencyCode;

    // --- Document References ---
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.BILLING_REFERENCE)
    private XmlBillingReference billingReference;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ORDER_REFERENCE)
    private XmlDocumentReference purchaseOrder;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.CONTRACT_DOCUMENT_REFERENCE)
    private XmlDocumentReference contract;

    /**
     * Used for ICV, PIH, QR
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ADDITIONAL_DOCUMENT_REFERENCE)
    private List<XmlAdditionalDocumentReference> additionalDocumentReferences;
}
