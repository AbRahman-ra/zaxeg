package sa.abrahman.zaxeg.core.generate.port.in.payload;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import sa.abrahman.zaxeg.core.generate.domain.constant.TaxExemptionCode;
import sa.abrahman.zaxeg.core.generate.domain.constant.TaxScheme;
import sa.abrahman.zaxeg.core.generate.domain.constant.VatCategory;

@UtilityClass
public class PayloadCommons {
    @Getter
    @RequiredArgsConstructor
    @NullMarked
    public static class Amount {
        private final BigDecimal value;
        private final Currency currency;
    }

    @Getter
    @Builder
    @NullUnmarked
    public static class AllowanceOrCharge {
        @NonNull
        private boolean isCharge;
        private BigDecimal percentage;

        @NonNull
        private Amount amount;
        private Amount baseAmount;
        private TaxCategory taxCategory;
    }

    @Getter
    @Builder
    @NullMarked
    public static class TaxCategory {
        private final VatCategory categoryCode;

        @Nullable
        private final TaxExemptionCode taxExemptionReasonCode;

        @Nullable
        private final String taxExemptionReason;

        private final TaxScheme scheme;
    }

    @Getter
    @Builder
    @NullMarked
    public static class TaxTotal {
        private Amount taxAmount;

        @Nullable
        private Amount roundingAmount;

        @Builder.Default
        private List<TaxSubtotal> taxSubtotal = List.of();
    }

    @Getter
    @Builder
    @NullMarked
    public static class TaxSubtotal {
        private Amount taxableAmount;
        private Amount taxAmount;
        private TaxCategory taxCategory;
    }
}
