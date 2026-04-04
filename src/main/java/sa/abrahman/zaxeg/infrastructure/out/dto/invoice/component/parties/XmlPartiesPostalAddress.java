package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.parties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlPartiesPostalAddress {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.STREET_NAME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String streetName;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ADDITIONAL_STREET_NAME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String additionalStreetName;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.BUILDING_NUMBER, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String buildingNumber;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.PLOT_IDENTIFICATION, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String plotIdentification; // ZATCA uses this for Additional Number

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.CITY_SUBDIVISION_NAME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String citySubdivisionName; // ZATCA uses this for District

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.CITY_NAME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String cityName;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.POSTAL_ZONE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String postalZone;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.COUNTRY_SUBENTITY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String countrySubentity; // ZATCA uses this for Province/State

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.COUNTRY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesCountry country;
}
