package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.UBLandZATCAConstants;

@Data
@Builder
public class AmountDto {
    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTRIBUTES.CURRENCY_ID)
    @Builder.Default
    private String currencyId = "SAR";

    @JacksonXmlText
    private String value;
}
