package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.line;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.AmountDto;

@Data
@Builder
public class PriceDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PRICE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto priceAmount;
}
