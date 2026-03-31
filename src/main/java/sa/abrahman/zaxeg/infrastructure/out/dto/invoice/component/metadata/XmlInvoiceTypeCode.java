package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.AllArgsConstructor;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlInvoiceTypeCode {

    /**
     * Maps to {@link sa.abrahman.zaxeg.core.model.invoice.metadata.InvoiceTypeTransactions InvoiceTypeTransactions}
     */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.NAME)
    private String name;

    /**
     * Maps to {@link sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType InvoiceDocumentType}
     */
    @JacksonXmlText
    private String value;
}
