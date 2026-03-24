package sa.abrahman.zaxeg.core.model.invoice.predefined;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VATCategory {
    STANDARD("S", new BigDecimal("15.00")),
    ZERO_RATED("Z", new BigDecimal("0.00")),
    EXEMPT("E", new BigDecimal("0.00")), // Healthcare, real estate, etc.
    OUT_OF_SCOPE("O", new BigDecimal("0.00")); // Out of KSA tax scope

    private final String code;
    private final BigDecimal rate;
}
