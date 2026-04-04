package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.parties;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlPartiesParty {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.PARTY_IDENTIFICATION, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private List<XmlPartiesPartyIdentification> partyIdentification;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.POSTAL_ADDRESS, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesPostalAddress postalAddress;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.PARTY_TAX_SCHEME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesPartyTaxScheme partyTaxScheme;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.PARTY_LEGAL_ENTITY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesPartyLegalEntity partyLegalEntity;
}
