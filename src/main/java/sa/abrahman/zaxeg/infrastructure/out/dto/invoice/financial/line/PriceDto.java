package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLandZATCAConstants;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.AmountDto;

@Data
@Builder
public class PriceDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PRICE_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto priceAmount;
}
