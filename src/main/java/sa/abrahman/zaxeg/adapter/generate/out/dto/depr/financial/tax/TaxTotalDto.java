package sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.tax;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.UBLandZATCAConstants;
import sa.abrahman.zaxeg.adapter.generate.out.dto.depr.financial.AmountDto;

@Data
@Builder
public class TaxTotalDto {
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.TAX_AMOUNT, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private AmountDto taxAmount;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CAC.TAX_SUBTOTAL, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private List<TaxSubtotalDto> taxSubtotals;
}
