package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.AmountDto;

@Data
@Builder
public class TaxSubtotalDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAXABLE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto taxableAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto taxAmount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_CATEGORY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxCategoryDto taxCategory;
}
