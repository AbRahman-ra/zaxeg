package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.VATCategory;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@UtilityClass
class Commons {
    @Getter
    @RequiredArgsConstructor
    static class Amount implements Payloadable<PayloadCommons.Amount, Void> {
        private final BigDecimal value;
        private final Currency currency;

        @Override
        public PayloadCommons.Amount toPayload(Void d) {
            return new PayloadCommons.Amount(value, currency);
        }
    }

    @Data
    static class AllowanceOrCharge implements Payloadable<PayloadCommons.AllowanceOrCharge, Currency> {
        @NotNull
        @Schema(title = "Charge Indicator", description = "pass it as false if the object represents a discount, and true if the object represents a fee", requiredMode = RequiredMode.REQUIRED, example = "false")
        private boolean isCharge;

        @Schema(title = "Allowance/Charge Percentage", requiredMode = RequiredMode.REQUIRED, example = "17.5")
        private BigDecimal percentage;

        @NotNull(message = InvoiceValidationRule.BR_41)
        @Schema(title = "Allowance/Charge Amount (in invoice currency)", requiredMode = RequiredMode.REQUIRED, example = "5.40")
        private BigDecimal amount;

        @Schema(title = "Amount before applying allowance/charge")
        private BigDecimal baseAmount;

        @Schema(title = "Allowance/charge VAT information")
        private TaxCategory taxCategory;

        @Override
        public PayloadCommons.AllowanceOrCharge toPayload(Currency currency) {
            return PayloadCommons.AllowanceOrCharge.builder()
                    .isCharge(isCharge)
                    .percentage(percentage)
                    .amount(new PayloadCommons.Amount(amount, currency))
                    .baseAmount(baseAmount == null ? null : new PayloadCommons.Amount(baseAmount, currency))
                    .taxCategory(taxCategory.toPayload(null))
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class TaxCategory implements Payloadable<PayloadCommons.TaxCategory, Void> {
        private final VATCategory categoryCode;
        private final TaxExemptionCode taxExemptionReasonCode;
        private final String taxExemptionReason;
        private final TaxScheme scheme;

        @Override
        public PayloadCommons.TaxCategory toPayload(Void additionalData) {
            return PayloadCommons.TaxCategory.builder()
                    .categoryCode(categoryCode)
                    .taxExemptionReason(taxExemptionReason)
                    .taxExemptionReasonCode(taxExemptionReasonCode)
                    .scheme(scheme)
                    .build();
        }
    }

    @Getter
    @Builder
    class TaxTotal implements Payloadable<PayloadCommons.TaxTotal, Currency> {
        private BigDecimal taxAmount;
        private BigDecimal roundingAmount;
        @Builder.Default
        private List<TaxSubtotal> taxSubtotal = List.of();

        @Getter
        @Builder
        static class TaxSubtotal implements Payloadable<PayloadCommons.TaxSubtotal, Currency> {
            private BigDecimal taxableAmount;
            private BigDecimal taxAmount;
            private TaxCategory taxCategory;

            @Override
            public PayloadCommons.TaxSubtotal toPayload(Currency currency) {
                return PayloadCommons.TaxSubtotal.builder()
                        .taxableAmount(new PayloadCommons.Amount(taxableAmount, currency))
                        .taxAmount(new PayloadCommons.Amount(taxAmount, currency))
                        .taxCategory(taxCategory == null ? null : taxCategory.toPayload(null))
                        .build();
            }
        }

        @Override
        public PayloadCommons.TaxTotal toPayload(Currency currency) {
            // nullables
            taxSubtotal = taxSubtotal == null ? List.of() : taxSubtotal;
            return PayloadCommons.TaxTotal.builder()
                    .taxAmount(new PayloadCommons.Amount(taxAmount, currency))
                    .roundingAmount(new PayloadCommons.Amount(roundingAmount, currency))
                    .taxSubtotal(taxSubtotal.stream().map(t -> t.toPayload(currency)).toList())
                    .build();
        }
    }
}
