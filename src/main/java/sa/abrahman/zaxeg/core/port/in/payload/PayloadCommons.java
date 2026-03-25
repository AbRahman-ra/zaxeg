package sa.abrahman.zaxeg.core.port.in.payload;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.VATCategory;

@UtilityClass
public class PayloadCommons {
    @Getter
    @RequiredArgsConstructor
    public static class Amount {
        private final BigDecimal value;
        private final Currency currency;
    }

    @Getter
    @Builder
    public static class AllowanceOrCharge {
        private boolean isCharge;
        private BigDecimal percentage;
        private PayloadCommons.Amount amount;
        private PayloadCommons.Amount baseAmount;
        private TaxCategory taxCategory;
    }

    @Getter
    @Builder
    public static class TaxCategory {
        private final VATCategory categoryCode;
        private final TaxExemptionCode taxExemptionReasonCode;
        private final String taxExemptionReason;
        private final TaxScheme scheme;
    }

    @Getter
    @Builder
    public static class TaxTotal {
        private Amount taxAmount;
        private Amount roundingAmount;
        private List<TaxSubtotal> taxSubtotal;
    }

    @Getter
    @Builder
    public static class TaxSubtotal {
        private Amount taxableAmount;
        private Amount taxAmount;
        private TaxCategory taxCategory;
    }
}
