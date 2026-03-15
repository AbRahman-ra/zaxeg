package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JacksonXmlRootElement(localName = UBLandZATCAConstants.TAG_INVOICE, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
public class UBLInvoiceDto {

    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTR_XMLNSCAC)
    @Builder.Default
    private String xmlnsCac = UBLandZATCAConstants.NAMESPACE_CAC;

    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTR_XMLNS_CBC)
    @Builder.Default
    private String xmlnsCbc = UBLandZATCAConstants.NAMESPACE_CBC;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_ID, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private String id;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_ISSUE_DATE, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private String issueDate;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_ISSUE_TIME, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private String issueTime;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_INVOICE_TYPE_CODE, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private InvoiceTypeCodeDto invoiceTypeCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_DOCUMENT_CURRENCY_CODE, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private String documentCurrencyCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_TAX_CURRENCY_CODE, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private String taxCurrencyCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_ACCOUNTING_SUPPLIER_PARTY, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private AccountingSupplierPartyDto supplierParty;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_LEGAL_MONETARY_TOTAL, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private LegalMonetaryTotalDto legalMonetaryTotal;
}
