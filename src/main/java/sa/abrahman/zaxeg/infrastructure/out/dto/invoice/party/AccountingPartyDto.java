package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;

@Data
@Builder
public class AccountingPartyDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.PARTY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private PartyDto party;
}
