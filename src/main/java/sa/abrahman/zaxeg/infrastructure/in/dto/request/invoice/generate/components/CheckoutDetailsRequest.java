package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate.components;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.predefined.PaymentMethod;
import sa.abrahman.zaxeg.core.port.in.payload.CheckoutDetailsPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Getter
@Schema(title = "Invoice Checkout Details", description = "Document-level totals, payment instructions, and global discounts/fees")
class CheckoutDetailsRequest implements Payloadable<CheckoutDetailsPayload, List<Currency>> {

    @NotNull(message = InvoiceValidationRule.BR_49)
    @Schema(title = "Payment Method", description = "BG16, BT-81: The means, expressed as code, for how a payment is expected to be or has been settled. Entries from the UNTDID 4461 code list", requiredMode = RequiredMode.REQUIRED, example = "IN_CASH")
    private PaymentMethod paymentMeansType;

    @Schema(description = "The payment terms, if mode of payment is credit. Free text", requiredMode = RequiredMode.REQUIRED, example = "Paid By Credit Card")
    private String paymentTerms = "";

    @Schema(title = "Payee Account Identifier", description = "The account number, IBAN, to which the transfer should be made. In the case of factoring this account is owned by the factor.")
    private String paymentAccountIdentifier = "";

    @Valid
    @Schema(title = "Document Level Allowances/Charges", description = "Global discounts or additional charges applied to the entire invoice", example = "SA9810000000000000000000")
    private List<InvoiceGenerationRequestCommons.AllowanceOrCharge> documentLevelAllowanceCharges = List.of();

    @Valid
    @Schema(description = "Pre-calculated totals provided by the client for strict validation against the engine's math")
    private LegalMonetaryTotals legalMonetaryTotals;

    @Valid
    @Schema(title = "Invoice Tax Totals", description = "Total VAT amounts for the invoice in the document's native currency.", requiredMode = RequiredMode.NOT_REQUIRED)
    private InvoiceGenerationRequestCommons.TaxTotal invoiceTaxTotals;

    @Valid
    @Schema(title = "Invoice Tax Totals (Accounting Currency)", description = "Total VAT amounts strictly converted to the accounting currency (SAR) as required by ZATCA.", requiredMode = RequiredMode.NOT_REQUIRED)
    private InvoiceGenerationRequestCommons.TaxTotal invoiceTaxTotalsInAccountingCurrency;

    @Override
    public CheckoutDetailsPayload toPayload(List<Currency> additionalData) {
        if (additionalData == null || additionalData.size() < 2)
            throw new IllegalArgumentException("additional data must have 2 currencues");

        Currency documentCurrency = additionalData.get(0);
        Currency taxCurrency = additionalData.get(1);

        return CheckoutDetailsPayload.builder().paymentMeansType(paymentMeansType).paymentTerms(paymentTerms)
                .paymentAccountIdentifier(paymentAccountIdentifier)
                .documentLevelAllowanceCharges(
                        documentLevelAllowanceCharges.stream().map(ac -> ac.toPayload(documentCurrency)).toList())
                .legalMonetaryTotals(legalMonetaryTotals.toPayload(documentCurrency))
                .invoiceTaxTotals(invoiceTaxTotals.toPayload(documentCurrency))
                .invoiceTaxTotalsInAccountingCurrency(invoiceTaxTotalsInAccountingCurrency.toPayload(taxCurrency))
                .build();
    }

    @Getter
    @Schema(description = "Client-provided totals for strict mathematical verification")
    static class LegalMonetaryTotals implements Payloadable<CheckoutDetailsPayload.LegalMonetaryTotals, Currency> {

        @Schema(title = "Total Line Extension Amount", description = "Sum of all Invoice line net amounts without VAT.", requiredMode = RequiredMode.REQUIRED, example = "1000.00")
        private BigDecimal lineExtensionAmount;

        /**
         * alias for {@code documentLevelAllowanceChargeTotalAmount}, renamed different than payload and domain for ease
         */
        @Schema(title = "Total Allowance/Charge Amount", description = "Sum of all document-level allowances/charges.", requiredMode = RequiredMode.NOT_REQUIRED, example = "50.00")
        private BigDecimal totalAllowanceChargeAmount;

        @Schema(title = "Tax Exclusive Amount", description = "The total amount of the Invoice without VAT.", requiredMode = RequiredMode.NOT_REQUIRED, example = "950.00")
        private BigDecimal taxExclusiveAmount;

        @Schema(title = "Total Inclusive Amount", description = "The total amount of the Invoice including VAT.", requiredMode = RequiredMode.REQUIRED, example = "1092.50")
        private BigDecimal totalAmountInclusive;

        @Schema(title = "Prepaid Amount", description = "The sum of amounts which have been paid in advance including VAT.", requiredMode = RequiredMode.NOT_REQUIRED, example = "0.00")
        private BigDecimal prepaidAmount;

        @Schema(title = "Payable Amount", description = "The outstanding amount that is requested to be paid (Inclusive Amount - Prepaid Amount).", requiredMode = RequiredMode.REQUIRED, example = "1092.50")
        private BigDecimal payableAmount;

        @Override
        public CheckoutDetailsPayload.LegalMonetaryTotals toPayload(Currency currency) {
            return CheckoutDetailsPayload.LegalMonetaryTotals.builder()
                    .lineExtensionAmount(new PayloadCommons.Amount(lineExtensionAmount, currency))
                    .documentLevelAllowanceChargeTotalAmount(
                            new PayloadCommons.Amount(totalAllowanceChargeAmount, currency))
                    .invoiceTotalAmountWithoutVAT(new PayloadCommons.Amount(taxExclusiveAmount, currency))
                    .totalInclusiveAmount(new PayloadCommons.Amount(totalAmountInclusive, currency))
                    .prepaidAmount(new PayloadCommons.Amount(prepaidAmount, currency))
                    .payableAmount(new PayloadCommons.Amount(payableAmount, currency)).build();
        }
    }
}
