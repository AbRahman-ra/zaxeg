package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.parties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlPartiesAccountingParty {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.PARTY, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesParty party;
}
