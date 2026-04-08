package sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.constant.UblInvoiceElements;

/** For the stamp */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataDocumentReferenceAttachment {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.EMBEDDED_DOCUMENT_BINARY_OBJECT)
    private XmlMetadataEmbeddedDocumentBinaryObject embeddedDocumentBinaryObject;
}
