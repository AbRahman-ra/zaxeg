package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.math.BigDecimal;
import java.util.Currency;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.validator.rules.BusinessIntegrityConstraintRule;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
class AllowanceOrCharge implements Payloadable<InvoiceGenerationPayload.AllowanceOrCharge, Currency> {
    @NotNull
    @Schema(title = "Charge Indicator", description = "pass it as false if the object represents a discount, and true if the object represents a fee", requiredMode = RequiredMode.REQUIRED, example = "false")
    private boolean isCharge;

    @Schema(title = "Allowance/Charge Percentage", requiredMode = RequiredMode.REQUIRED, example = "17.5")
    private Double percentage;

    @NotNull(message = BusinessIntegrityConstraintRule.BR_41)
    @Schema(title = "Allowance/Charge Amount (in invoice currency)", requiredMode = RequiredMode.REQUIRED, example = "5.40")
    private BigDecimal amount;

    @Schema(title = "Amount before applying allowance/charge")
    private BigDecimal baseAmount;

    @Schema(title = "Allowance/charge VAT information")
    private TaxCategory taxCategory;

    @Override
    public InvoiceGenerationPayload.AllowanceOrCharge toPayload(Currency currency) {
        return InvoiceGenerationPayload.AllowanceOrCharge.builder()
        .isCharge(isCharge)
        .percentage(percentage)
        .amount(new InvoiceGenerationPayload.Amount(amount, currency))
        .baseAmount(baseAmount == null ? null : new InvoiceGenerationPayload.Amount(baseAmount, currency))
        .taxCategory(taxCategory.toPayload(null))
        .build();
    }
}
