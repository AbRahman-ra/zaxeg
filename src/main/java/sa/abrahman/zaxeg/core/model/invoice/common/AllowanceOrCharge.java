package sa.abrahman.zaxeg.core.model.invoice.common;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AllowanceOrCharge {
    /**
     * BG-20: An indicator that this AllowanceCharge describes a discount. The value
     * of this tag indicating discount (Allowance) must be {@code false}.
     */
    private boolean isCharge;

    /**
     * BG-20, BT-94: The percentage that may be used, in conjunction with the
     * document level allowance base amount, to calculate the document level
     * allowance amount.
     * @implNote Used {@code BigDecimal} instead of {@code Double} to avoid floating point errors
     */
    private BigDecimal percentage;

    /**
     * <ul>
     * <li>BG-20, BT-92: The amount of an allowance, without VAT.</li>
     * <li>BG-20, BT-5: Currency for line allowance amount</li>
     * </ul>
     */
    private Amount amount;

    /**
     * <ul>
     * <li>BG-20, BT-93: The base amount that may be used, in conjunction with the Invoice line allowance percentage, to calculate the Invoice line allowance amount.</li>
     * <li>BG-20, BT-5: Currency for invoice line allowance base amount</li>
     * </ul>
     */
    private Amount baseAmount;

    /**
     * <ul>
     * <li>BG-20, BT-95: A coded identification of what VAT category applies to the document level allowance.</li>
     * <li>BG-20, BT-96: The VAT rate, represented as percentage that applies to the document level allowance.</li>
     * <li>KSA-21: Mandatory element. Use {@code VAT}</li>
     * </ul>
     */
    private TaxCategory taxCategory;
}
