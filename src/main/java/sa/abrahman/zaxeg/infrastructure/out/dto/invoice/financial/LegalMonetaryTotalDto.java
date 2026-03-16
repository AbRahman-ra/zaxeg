package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;

@Data
@Builder
public class LegalMonetaryTotalDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.LINE_EXTENSION_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto lineExtensionAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_EXCLUSIVE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto taxExclusiveAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_INCLUSIVE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto taxInclusiveAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PREPAID_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto prepaidAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PAYABLE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto payableAmount;
}
