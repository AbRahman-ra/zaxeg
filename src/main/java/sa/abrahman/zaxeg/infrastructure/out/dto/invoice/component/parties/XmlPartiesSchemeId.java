package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.parties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlPartiesSchemeId {
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.SCHEME_ID)
    private String schemeID;

    @JacksonXmlText
    private String value;
}
