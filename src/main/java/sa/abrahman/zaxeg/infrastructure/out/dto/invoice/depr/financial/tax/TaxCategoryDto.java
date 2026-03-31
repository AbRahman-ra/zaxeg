package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial.tax;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.UBLandZATCAConstants;

@Data
@Builder
public class TaxCategoryDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String id;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PERCENT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String percent;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_EXEMPTION_REASON_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String taxExemptionReasonCode;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_EXEMPTION_REASON, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String taxExemptionReason;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_SCHEME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxSchemeDto taxScheme;

    @Data
    @Builder
    public static class TaxSchemeDto {
        @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
        @Builder.Default
        private String id = "VAT";
    }
}
