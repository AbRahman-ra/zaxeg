package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.line;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.tax.TaxCategoryDto;

@Data
@Builder
public class ItemDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.NAME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String name;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.CLASSIFIED_TAX_CATEGORY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxCategoryDto classifiedTaxCategory;
}
