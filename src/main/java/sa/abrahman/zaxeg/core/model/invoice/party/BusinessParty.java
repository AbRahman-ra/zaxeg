package sa.abrahman.zaxeg.core.model.invoice.party;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessParty {
    private String registrationName;
    private String vatNumber;
    private String commercialRegistrationNumber; // CRN
    private Address address;
}
