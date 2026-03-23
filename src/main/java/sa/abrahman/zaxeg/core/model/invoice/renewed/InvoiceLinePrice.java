package sa.abrahman.zaxeg.core.model.invoice.renewed;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceLinePrice {
    /**
     * <ul>
     * <li>BG-29, BT-146:
     * <ul>
     * <li>The price of an item, exclusive of VAT, after subtracting item price discount.</li>
     * <li>The Item net price has to be equal with the Item gross price minus the Item price discount.</li>
     * </ul>
     * </li>
     *
     * <li>BH-29, BT-05: Currency for item net price</li>
     * </ul>
     */
    private Amount priceAmount;

    /**
     * <ul>
     * <li>BG-29, BT-149: The number of item units to which the price applies.</li>
     * <li>BG-29, BT-150: Item price base quantity unit code</li>
     * </ul>
     */
    private Quantity baseQualtity;

    /**
     * <ul>
     * <li>BG-29: An indicator that this AllowanceCharge describes a discount. The value of this tag indicating discount (Allowance) must be "false". This is in the context of the line item price level (not the line item level).</li>
     * <li> BG-29, BT-147:
     * <ul>
     * <li>The total discount subtracted from the Item gross price to calculate the Item net price.</li>
     * <li>Only applies if the discount is provided per unit and if it is not included in the Item gross price.</li>
     * <li>This is in the context of the line item price level (not the line item level).</li>
     * </ul>
     * </li>
     * <li>BG-29, BT-05: Currency for item price discount</li>
     * <li>BG-29, BT-148: The unit price, exclusive of VAT, before subtracting Item price discount.</li>
     * <li>BG-29, BT-05: Currency for item gross prices. This is in the context of the line item price level (not the line item level).</li>
     * </ul>
     *
     * @implNote ZATCA cardinality for this object is {@code 0..1}, so wrapping it in a list is not feasable
     *
     */
    private AllowanceOrCharge allowanceCharge;
}
