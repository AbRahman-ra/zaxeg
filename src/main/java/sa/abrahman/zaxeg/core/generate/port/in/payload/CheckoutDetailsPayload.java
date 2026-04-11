package sa.abrahman.zaxeg.core.generate.port.in.payload;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.generate.domain.constant.PaymentMethod;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PayloadCommons.TaxTotal;

@Getter
@Builder
@NullMarked
public class CheckoutDetailsPayload {
    private final PaymentMethod paymentMeansType;
    private final String paymentTerms;
    private final String paymentAccountIdentifier;

    // --- Document Level Discounts / Fees ---
    private final List<PayloadCommons.AllowanceOrCharge> documentLevelAllowanceCharges;

    /**
     * Optional: Client-Provided Financials for Strict Validation.
     * <p>
     * If the API caller provides these, your Math Engine will calculate the invoice and then assert that its math
     * matches the caller's math.
     * </p>
     */
    private final LegalMonetaryTotals legalMonetaryTotals;
    private final TaxTotal invoiceTaxTotals;
    private final TaxTotal invoiceTaxTotalsInAccountingCurrency;

    @Getter
    @Builder
    @NullMarked
    public static class LegalMonetaryTotals {
        private final PayloadCommons.Amount lineExtensionAmount;
        private final PayloadCommons.Amount documentLevelAllowanceChargeTotalAmount;
        private final PayloadCommons.Amount invoiceTotalAmountWithoutVAT;
        private final PayloadCommons.Amount totalInclusiveAmount;
        private final PayloadCommons.Amount prepaidAmount;
        private final PayloadCommons.Amount payableAmount;
    }
}
