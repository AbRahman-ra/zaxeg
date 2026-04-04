package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

/**
 * Maps to {@link sa.abrahman.zaxeg.core.model.invoice.wrapper.Metadata#billingReference Metadata.billingReference}
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataBillingReference {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.INVOICE_DOCUMENT_REFERENCE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private XmlMetadataDocumentReference invoiceDocumentReference;
}
