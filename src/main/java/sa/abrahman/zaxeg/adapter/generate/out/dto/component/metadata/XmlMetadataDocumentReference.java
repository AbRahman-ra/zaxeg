package sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.constant.UblInvoiceElements;

/**
 * Maps to {@link sa.abrahman.zaxeg.core.generate.domain.model.invoice.metadata.DocumentReference DocumentReference}
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataDocumentReference {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ID, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String id;
}
