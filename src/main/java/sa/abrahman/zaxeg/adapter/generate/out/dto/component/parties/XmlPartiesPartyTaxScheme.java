package sa.abrahman.zaxeg.adapter.generate.out.dto.component.parties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sa.abrahman.zaxeg.adapter.generate.out.constant.UblInvoiceElements;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlPartiesPartyTaxScheme {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.COMPANY_ID, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String companyId;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.TAX_SCHEME, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlPartiesTaxScheme taxScheme;
}
