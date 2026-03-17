package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;

@Data
@Builder
public class QuantityDto {

    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTRIBUTES.UNIT_CODE)
    @Builder.Default
    private String unitCode = "PCE";

    @JacksonXmlText
    private String value;
}
