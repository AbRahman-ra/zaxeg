package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegalMonetaryTotalDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_LINE_EXTENSION_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private AmountDto lineExtensionAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_TAX_EXCLUSIVE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private AmountDto taxExclusiveAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_TAX_INCLUSIVE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private AmountDto taxInclusiveAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_PREPAID_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private AmountDto prepaidAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_PAYABLE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private AmountDto payableAmount;
}
