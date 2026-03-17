package sa.abrahman.zaxeg.core.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public enum TaxCategory {
    STANDARD("S", new BigDecimal("15.00")),
    ZERO_RATED("Z", new BigDecimal("0.00")),
    EXEMPT("E", new BigDecimal("0.00")), // Healthcare, real estate, etc.
    OUT_OF_SCOPE("O", new BigDecimal("0.00")); // Out of KSA tax scope

    private final String code;
    private final BigDecimal rate;

    // We will use setter methods when instantiating the specific exemption reason
    @Setter private String exemptionReasonCode; // e.g., "VATEX-SA-32"
    @Setter private String exemptionReasonText; // e.g., "Export of goods"
}
