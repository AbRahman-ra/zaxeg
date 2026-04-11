package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.meta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;

@Data
@Builder
public class DeliveryDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ACTUAL_DELIVERY_DATE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String actualDeliveryDate;
}
