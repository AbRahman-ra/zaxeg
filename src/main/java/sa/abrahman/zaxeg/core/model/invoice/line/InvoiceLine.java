package sa.abrahman.zaxeg.core.model.invoice.line;


import java.util.List;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.common.AllowanceOrCharge;
import sa.abrahman.zaxeg.core.model.invoice.common.Amount;
import sa.abrahman.zaxeg.core.model.invoice.common.TaxTotal;

@Getter
@Builder
public class InvoiceLine {
    /**
     * BG25, BT-126: A unique identifier for the individual line within the Invoice. Usually a sequential number (1, 2, 3...).
     */
    private String id;

    /**
     * BG-25, BT-129: The quantity of items (goods or services) that is charged in the Invoice line.
     * BG-25, BT-130: The unit of measure that applies to the invoiced quantity.
    */
    private Quantity quantity;

    /**
     * <ul>
     * <li>BG-25, BT-131:
     * <ul>
     * <li>The total amount of the Invoice line, including allowances (discounts). It is the item net price multiplied with the quantity.</li>
     * <li>The amount is “net” without VAT.</li>
     * </ul>
     * </li>
     *
     * <li>Currency for invoice line net amount</li>
     * </ul>
    */
    private Amount netAmount;

    /**
     * <ul>
     * <li>BG-27:
     * <ul>
     * <li>An indicator that this AllowanceCharge describes a discount.</li>
     * <li>The value of this tag indicating discount (Allowance) must be {@code false}.</li>
     * </ul>
     * </li>
     * <li>BG-27, BT-138: The percentage that may be used, in conjunction with the Invoice line allowance base amount, to calculate the Invoice line allowance amount.</li>
     * <li>BG-27, BT-136: The amount of an allowance, without VAT.</li>
     * <li>BG-27, BT-05: Currency for line allowance amount</li>
     * <li>BG-27, BT-137: The base amount that may be used, in conjunction with the Invoice line allowance percentage, to calculate the Invoice line allowance amount.</li>
     * <li>BG-27, BT-05: Currency for invoice line allowance base amount</li>
     * </ul>
     */
    private List<AllowanceOrCharge> allowanceCharges;

    /**
     * <ul>
     * <li>KSA-11: VAT amount  as per Article 53</li>
     * <li>BT-05: Currency for VAT line amount</li>
     * <li>KSA-12: Line amount inclusive VAT</li>
     * <li>BT-05: Currency for line amount inclusive VAT</li>
     * </ul>
     *
     * @implNote The KSA-12 and its currency are using type {@code roundingAmount} property
     */
    private TaxTotal vatLineAmount;

    /** Invoice line item data (without prices) */
    private InvoiceLineItem item;

    private InvoiceLinePrice price;
}
