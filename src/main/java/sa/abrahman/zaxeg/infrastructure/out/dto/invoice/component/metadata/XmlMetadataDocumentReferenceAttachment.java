package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

/** For the stamp */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataDocumentReferenceAttachment {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.EMBEDDED_DOCUMENT_BINARY_OBJECT)
    private XmlMetadataEmbeddedDocumentBinaryObject embeddedDocumentBinaryObject;
}
