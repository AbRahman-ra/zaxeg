package sa.abrahman.zaxeg.core.model.invoice.old.party;

import lombok.Builder;
import lombok.Getter;

/**
 * @deprecated
 */
@Deprecated(forRemoval = true)
@Getter
@Builder
public class BusinessParty {
    private String registrationName;
    private String vatNumber;
    private String commercialRegistrationNumber; // CRN
    private Address address;
}
