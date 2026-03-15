package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountingSupplierPartyDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAG_PARTY, namespace = UBLandZATCAConstants.NAMESPACE_ROOT)
    private PartyDto party;
}
