package sa.abrahman.zaxeg.core.generate.domain.model.invoice.line;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.MeasuringUnit;

@Getter
@RequiredArgsConstructor
public class Quantity {
    private final BigDecimal count;
    private final MeasuringUnit unit;

    public static Quantity of(BigDecimal count, MeasuringUnit unit) {
        return new Quantity(count, unit);
    }
}
