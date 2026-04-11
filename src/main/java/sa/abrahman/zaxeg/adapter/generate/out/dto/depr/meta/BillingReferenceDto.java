package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.meta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class BillingReferenceDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.INVOICE_DOCUMENT_REFERENCE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private DocumentReferenceDto invoiceDocumentReference;
}
