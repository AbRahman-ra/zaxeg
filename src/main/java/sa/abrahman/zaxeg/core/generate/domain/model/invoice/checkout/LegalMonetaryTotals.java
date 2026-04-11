package sa.abrahman.zaxeg.core.generate.domain.model.invoice.checkout;

import org.jspecify.annotations.NullMarked;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.common.Amount;

@Getter
@Builder
@NullMarked
public class LegalMonetaryTotals {
    /**
     * <ul>
     * <li>BG-22, BT-106: Sum of all Invoice line net amounts in the Invoice without VAT</li>
     * <li>BG-22, BT-05: Currency for invoicetotal amount</li>
     * </ul>
     */
    private Amount lineExtensionAmount;

    /**
     * <li>BG-22, BT-107:
     * <ul>
     * <li>Sum of all allowances on document level in the Invoice.</li>
     * <li>Allowances on line level are included in the Invoice line net amount which is summed up into the Sum of Invoice line net amount.</li>
     * </ul>
     * </li>
     *
     * <li>BG-22, BT-05: Currency for invoice total amount without VAT</li>
     */
    private Amount documentLevelAllowanceChargeTotalAmount;

    /**
     * Also Known As {@code TaxExclusiveAmount}
     * <ul>
     * <li>BG-22, BT-109: The total amount of the Invoice without VAT.</li>
     * <li>BG-22, BT-05: Currency for invoice total amount without VAT.</li>
     * </ul>
     */
    private Amount invoiceTotalAmountWithoutVAT;

    /**
     * <ul>
     * <li>BG-22, BT-112:
     * <ul>
     * <li>The total amount of the Invoice with VAT.</li>
     * <li>The Invoice total amount with VAT is the Invoice total amount without VAT plus the Invoice total VAT amount.</li>
     * </ul>
     * </li>
     *
     * <li>BG-22, BT-05: Currency for invoice total amount with VAT</li>
     * </ul>
     */
    private Amount totalInclusiveAmount;

    /**
     * <ul>
     * <li>BG-22, BT-113:
     * <ul>
     * <li>The sum of amounts which have been paid in advance including VAT.</li>
     * <li>This amount is subtracted from the invoice total amount with VAT to calculate the amount due for payment.</li>
     * </ul>
     * </li>
     *
     * <li>BG-22, BT-05: Currency for paid amount</li>
     * </ul>
     * @implNote Aliased as Paid Amount
     */
    private Amount prepaidAmount;

    /**
     * <ul>
     * <li>BG-22, BT-115:
     * <ul>
     * <li>The outstanding amount that is requested to be paid.</li>
     * <li>This amount is the Invoice total amount with VAT minus the paid amount that has been paid in advance. The amount is zero in case of a fully paid Invoice.</li>
     * </ul>
     * </li>
     *
     * <li>BG-22, BT-05: Currency for paid amount</li>
     * </ul>
     * @implNote Aliased as Amount Due for Payment
     */
    private Amount payableAmount;
}
