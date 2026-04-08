package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class CountryDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.IDENTIFICATION_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String identificationCode;
}
