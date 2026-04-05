package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate.components;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.VATCategory;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.core.validator.rule.ImplicitRules;
import sa.abrahman.zaxeg.core.validator.rule.KsaRules;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@UtilityClass
class InvoiceGenerationRequestCommons {
    @Getter
    @NullMarked
    static class AllowanceOrCharge implements Payloadable<PayloadCommons.AllowanceOrCharge, Currency> {
        @NotNull(message = InvoiceValidationRule.BR_KSA_F_02)
        @Schema(title = "Charge Indicator", description = "pass it as false if the object represents a discount, and true if the object represents a fee", requiredMode = RequiredMode.REQUIRED, example = "false")
        private boolean isCharge;

        @Nullable
        @DecimalMax(value = "100.00")
        @DecimalMin(value = "0.00")
        @Schema(title = "Allowance/Charge Percentage", requiredMode = RequiredMode.REQUIRED, example = "17.5")
        private BigDecimal percentage;

        @NotNull(message = InvoiceValidationRule.BR_41)
        @Positive(message = InvoiceValidationRule.BR_KSA_F_04)
        @Schema(title = "Allowance/Charge Amount (in invoice currency)", requiredMode = RequiredMode.REQUIRED, example = "5.40")
        private BigDecimal amount;

        @Nullable
        @Schema(title = "Amount before applying allowance/charge")
        private BigDecimal baseAmount;

        @Valid
        @Nullable
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
    @AllArgsConstructor
    @NullUnmarked
    static class TaxCategory implements Payloadable<PayloadCommons.TaxCategory, Void> {
        @NotNull(message = KsaRules.BR_KSA_18)
        @NonNull
        private VATCategory categoryCode;

        private TaxExemptionCode taxExemptionReasonCode;
        private String taxExemptionReason;
        private TaxScheme scheme;

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
    @NullUnmarked
    class TaxTotal implements Payloadable<PayloadCommons.TaxTotal, Currency> {
        @NotNull(message = ImplicitRules.TAX_AMOUNT_NOT_NULL)
        private BigDecimal taxAmount;
        private BigDecimal roundingAmount;

        @Valid
        @Builder.Default
        private List<TaxSubtotal> taxSubtotal = List.of();

        @Getter
        @Builder
        @NullMarked
        static class TaxSubtotal implements Payloadable<PayloadCommons.TaxSubtotal, Currency> {
            @NotNull(message = ImplicitRules.TAXABLE_AMOUNT_NOT_NULL)
            private BigDecimal taxableAmount;

            @NotNull(message = ImplicitRules.TAX_AMOUNT_NOT_NULL)
            private BigDecimal taxAmount;

            @Valid
            @Nullable
            private TaxCategory taxCategory;

            @Override
            public PayloadCommons.TaxSubtotal toPayload(Currency currency) {
                return PayloadCommons.TaxSubtotal.builder()
                        .taxableAmount(new PayloadCommons.Amount(taxableAmount, currency))
                        .taxAmount(new PayloadCommons.Amount(taxAmount, currency))
                        .taxCategory(taxCategory == null ? null : taxCategory.toPayload())
                        .build();
            }
        }

        @Override
        public PayloadCommons.TaxTotal toPayload(Currency currency) {
            // nullables
            taxSubtotal = taxSubtotal == null ? List.of() : taxSubtotal;
            return PayloadCommons.TaxTotal.builder().taxAmount(new PayloadCommons.Amount(taxAmount, currency))
                    .roundingAmount(new PayloadCommons.Amount(roundingAmount, currency))
                    .taxSubtotal(taxSubtotal.stream().map(t -> t.toPayload(currency)).toList()).build();
        }
    }
}
