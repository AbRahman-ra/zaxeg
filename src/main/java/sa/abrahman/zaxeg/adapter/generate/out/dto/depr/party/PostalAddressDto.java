package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class PostalAddressDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.BUILDING_NUMBER, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String buildingNumber;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.STREET_NAME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String streetName;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.CITY_SUBDIVISION_NAME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String citySubdivisionName; // District in ZATCA

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.CITY_NAME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String cityName;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.POSTAL_ZONE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String postalZone;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.COUNTRY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private CountryDto country;
}
