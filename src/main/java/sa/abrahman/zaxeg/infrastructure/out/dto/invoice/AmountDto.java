package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmountDto {
    @JacksonXmlProperty(isAttribute = true, localName = "currencyID")
    @Builder.Default
    private String currencyId = "SAR";

    @JacksonXmlText
    private String value;
}
