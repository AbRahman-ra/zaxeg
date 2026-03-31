package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.meta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.UBLandZATCAConstants;

@Data
@Builder
public class InvoiceTypeCodeDto {
    /**
     * The invoice subtype (standard / simplified)
     */
    @JacksonXmlProperty(isAttribute = true, localName = UBLandZATCAConstants.ATTRIBUTES.NAME)
    private String name;

    /**
     * The document type (invoice / debit / credit / etc...)
     */
    @JacksonXmlText
    private String value;
}
