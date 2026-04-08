package sa.abrahman.zaxeg.adapter.generate.out.dto.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.constant.UblInvoiceElements;

/**
 * To be used in phase ii (icv, pih, qr, etc....)
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataAdditionalDocumentReference {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ID)
    /** e.g., "ICV", "PIH", "QR" */
    private String id;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.UUID)
    /** Used for ICV */
    private String uuid;

    /** Used for PIH and QR */
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CAC.ATTACHMENT)
    private XmlMetadataDocumentReferenceAttachment attachment;
}
