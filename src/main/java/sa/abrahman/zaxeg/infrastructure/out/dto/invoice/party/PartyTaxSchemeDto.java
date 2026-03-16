package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.TaxCategoryDto.TaxSchemeDto;

@Data
@Builder
public class PartyTaxSchemeDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.COMPANY_ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String companyId; // VAT Number

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_SCHEME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private TaxSchemeDto taxScheme;
}
