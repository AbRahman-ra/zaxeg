package sa.abrahman.zaxeg.core.model.invoice.line;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.predefined.MeasuringUnit;

@Getter
@Builder
public class Quantity {
    private MeasuringUnit unit;
    private BigDecimal count;
}
