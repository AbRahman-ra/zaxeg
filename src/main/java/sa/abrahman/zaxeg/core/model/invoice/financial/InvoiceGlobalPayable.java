package sa.abrahman.zaxeg.core.model.invoice.financial;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Builder;
import lombok.Data;

/**
 * A container for document allowances or charges
 */
@Data
@Builder
public class InvoiceGlobalPayable {
    private boolean isCharge; // true = Charge (Fee), false = Allowance (Discount)
    private String reason;    // e.g., "VIP Discount" or "Delivery Fee"
    private BigDecimal amount; // The base amount of the discount/fee
    private TaxCategory taxCategory; // The tax rate applied to this discount/fee

    // ZATCA strictly requires exemption reasons if the allowance/charge is Zero-Rated or Exempt
    private String exemptionReasonCode;
    private String exemptionReasonText;

    @Deprecated(forRemoval = true)
    /**
     * @deprecated
     * @return
     */
    public BigDecimal getTaxAmount() {
        if (amount == null || taxCategory == null) return BigDecimal.ZERO;
        // tax = amount * (rate / 100)
        return amount.multiply(taxCategory.getRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
