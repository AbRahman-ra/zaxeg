package sa.abrahman.zaxeg.core.model.invoice.old.financial;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentFinancials {
    /**
     * Sum of all InvoiceLine.netPrice. (Tax Exclusive)
     */
    private BigDecimal totalLineExtensionAmount;

    /**
     * Total VAT amount across the entire invoice
     */
    private BigDecimal totalTaxAmount;

    /**
     * Required by BR-53 if taxCurrency != documentCurrency
     */
    private BigDecimal totalTaxAmountInAccountingCurrency;

    /**
     * totalLineExtensionAmount + totalTaxAmount
     */
    private BigDecimal totalAmountInclusive;

    /**
     * Any amount paid in advance (usually 0.00 for standard B2B)
     */
    private BigDecimal prepaidAmount;

    /**
     * totalAmountInclusive - prepaidAmount (This is what the buyer actually owes)
     */
    private BigDecimal payableAmount;

    /**
     * totalLineExtensionAmount - documentAllowances + documentCharges
     * (This is the true Taxable Base)
     */
    private BigDecimal taxExclusiveAmount;
}
