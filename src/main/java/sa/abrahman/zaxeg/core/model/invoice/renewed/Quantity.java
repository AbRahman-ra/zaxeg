package sa.abrahman.zaxeg.core.model.invoice.renewed;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.old.financial.MeasuringUnit;

@Getter
@Builder
public class Quantity {
    private MeasuringUnit unit;
    private BigDecimal count;
}
