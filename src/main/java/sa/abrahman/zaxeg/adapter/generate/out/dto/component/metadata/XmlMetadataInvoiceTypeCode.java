package sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.AllArgsConstructor;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.constant.UblInvoiceElements;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataInvoiceTypeCode {

    /**
     * Maps to {@link sa.abrahman.zaxeg.core.generate.domain.model.invoice.metadata.InvoiceTypeTransactions
     * InvoiceTypeTransactions}
     */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.NAME)
    private String name;

    /**
     * Maps to {@link sa.abrahman.zaxeg.core.generate.domain.constant.InvoiceDocumentType InvoiceDocumentType}
     */
    @JacksonXmlText
    private String value;
}
