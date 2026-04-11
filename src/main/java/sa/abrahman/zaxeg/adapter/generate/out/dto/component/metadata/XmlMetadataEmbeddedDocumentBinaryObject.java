package sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.constant.UblInvoiceElements;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataEmbeddedDocumentBinaryObject {
    /** e.g., "text/plain" */
    @JacksonXmlProperty(isAttribute = true, localName = UblInvoiceElements.ATTRIBUTES.MIME_CODE)
    private String mimeCode;

    /** The Base64 hash */
    @JacksonXmlProperty(isAttribute = false)
    private String value;
}
