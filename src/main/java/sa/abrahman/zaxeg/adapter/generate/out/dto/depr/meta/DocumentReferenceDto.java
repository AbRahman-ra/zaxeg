package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.meta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class DocumentReferenceDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ID, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String id;
}
