package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.line.InvoiceLineDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.tax.TaxTotalDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.meta.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.party.AccountingPartyDto;

@Data
@Builder
@JacksonXmlRootElement(localName = UBLandZATCAConstants.TAGS.INVOICE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
public class ZATCAInvoiceDto {

    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTRIBUTES.XMLNS.CAC)
    @Builder.Default
    private String xmlnsCac = UBLandZATCAConstants.NAMESPACES.CAC;

    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTRIBUTES.XMLNS.CBC)
    @Builder.Default
    private String xmlnsCbc = UBLandZATCAConstants.NAMESPACES.CBC;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String id;

    @Builder.Default
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PROFILE_ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String profileId = UBLandZATCAConstants.DEFAULTS.PROFILE_ID;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.UUID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String uuid;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ISSUE_DATE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String issueDate;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ISSUE_TIME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String issueTime;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.INVOICE_TYPE_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private InvoiceTypeCodeDto invoiceTypeCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.BILLING_REFERENCE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private BillingReferenceDto billingReference;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.DOCUMENT_CURRENCY_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String documentCurrencyCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_CURRENCY_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String taxCurrencyCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.ACCOUNTING_SUPPLIER_PARTY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AccountingPartyDto supplierParty;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.ACCOUNTING_CUSTOMER_PARTY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AccountingPartyDto buyerParty;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.DELIVERY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private DeliveryDto delivery;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.PAYMENT_MEANS, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private PaymentMeansDto paymentMeans;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.ALLOWANCE_CHARGE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private List<InvoiceGlobalPayableDto> allowanceCharges;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_TOTAL, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private List<TaxTotalDto> taxTotals;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.LEGAL_MONETARY_TOTAL, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private LegalMonetaryTotalDto legalMonetaryTotal;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.INVOICE_LINE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private List<InvoiceLineDto> invoiceLines;
}
