package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.line;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.UBLandZATCAConstants;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.AmountDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.tax.TaxTotalDto;

@Data
@Builder
public class InvoiceLineDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String id;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.INVOICED_QUANTITY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private QuantityDto invoicedQuantity;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.LINE_EXTENSION_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto lineExtensionAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_TOTAL, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxTotalDto taxTotal;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.ITEM, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private ItemDto item;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.PRICE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private PriceDto price;
}
