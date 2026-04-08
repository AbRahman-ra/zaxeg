package sa.abrahman.zaxeg.core.generate.domain.model.invoice.common;

import java.math.BigDecimal;
import java.util.Currency;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@NullMarked
public class Amount {
    private final BigDecimal value;
    private final Currency currency;

    public static Amount of(@NonNull BigDecimal value, @NonNull Currency currency) {
        return new Amount(value, currency);
    }
}
