package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.meta;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.UBLandZATCAConstants;

@Data
@Builder
public class DeliveryDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.ACTUAL_DELIVERY_DATE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String actualDeliveryDate;
}
