package sa.abrahman.zaxeg.core.model.invoice.party;

import java.util.Locale;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Address {
     /** Address line 1 - the main address line in an address */
     private final String street;

     /** Address line 2 - an additional address line in an address that can be used to give further details supplementing the main line. */
     private final String additionalStreet;

     /** Address building number */
     private final String buildingNumber;

     /** Address additional number */
     private final String additionalNumber;

     /** The common name of the city, town or village, where the Party's address is located. */
     private final String city;

     /** Post code */
     private final String postalCode;

     /** Country subdivision */
     private final String provinceOrState;

     /** The name of the subdivision of the Party city, town, or village in which its address is located, such as the name of its district or borough. */
     private final String district;

     /** Country code */
     private final Locale country;
}
