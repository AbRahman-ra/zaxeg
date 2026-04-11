package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class PartyLegalEntityDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.REGISTRATION_NAME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String registrationName;
}
