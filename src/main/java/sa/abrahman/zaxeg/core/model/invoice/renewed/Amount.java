package sa.abrahman.zaxeg.core.model.invoice.renewed;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Amount {
    private final BigDecimal value;
    private final Currency currency;

    public static Amount of(BigDecimal value, Currency currency) {
        return new Amount(value, currency);
    }
}
