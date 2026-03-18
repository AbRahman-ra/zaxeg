package sa.abrahman.zaxeg.core.model.invoice.party;

import java.util.Locale;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Address {
    private String buildingNumber;
    private String streetName;
    private String district;
    private String city;
    private String postalCode;
    private String additionalNumber;

    @Builder.Default
    private Locale country = Locale.of("", "SA");
}
