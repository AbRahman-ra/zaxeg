package sa.abrahman.zaxeg.core.model.invoice.party;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Party {
    private String name;
    private PartyTaxScheme identification;
    private List<PartyIdentification> otherIds;
    private Address address;
}
