package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.party;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class PartyDto {

    // VAT or CRN
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ENDPOINT_ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private EndPointIdDto endpointId;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.POSTAL_ADDRESS, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private PostalAddressDto postalAddress;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.PARTY_TAX_SCHEME, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private PartyTaxSchemeDto partyTaxScheme;

    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.PARTY_LEGAL_ENTITY, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private PartyLegalEntityDto partyLegalEntity;
}
