package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.tax.TaxCategoryDto.TaxSchemeDto;

@Data
@Builder
public class PartyTaxSchemeDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.COMPANY_ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String companyId; // VAT Number

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_SCHEME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxSchemeDto taxScheme;
}
