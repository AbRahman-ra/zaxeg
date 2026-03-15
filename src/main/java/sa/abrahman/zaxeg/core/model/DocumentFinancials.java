package sa.abrahman.zaxeg.core.model;

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
}
