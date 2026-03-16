package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;

@Data
@Builder
public class CountryDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.IDENTIFICATION_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String identificationCode;
}
