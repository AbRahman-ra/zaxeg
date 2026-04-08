package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.tax.TaxCategoryDto;

/**
 * A container for document allowances or charges
 */
@Data
@Builder
public class InvoiceGlobalPayableDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.CHARGE_INDICATOR, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private boolean chargeIndicator;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ALLOWANCE_CHARGE_REASON, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String allowanceChargeReason;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto amount;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_CATEGORY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxCategoryDto taxCategory;
}
