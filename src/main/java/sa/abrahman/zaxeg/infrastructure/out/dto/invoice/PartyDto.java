package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartyDto {

    // VAT number
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_ENDPOINT_ID, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private String endpointId;
}
