package sa.abrahman.zaxeg.core.model.invoice.renewed;

import java.math.BigDecimal;
import java.util.Currency;

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
     */
    private Double percentage;

    /**
     * BG-20, BT-92: The amount of an allowance, without VAT.
     */
    private BigDecimal amount;

    /**
     * BG-20, BT-5: Currency for line allowance amount
     */
    private Currency amountCurrency;

    /**
     * BG-20, BT-93: The base amount that may be used, in conjunction with the
     * Invoice line allowance percentage, to calculate the Invoice line allowance
     * amount.
     */
    private BigDecimal baseAmount;

    /**
     * BG-20, BT-5: Currency for invoice line allowance base amount
     */
    private Currency baseAmountCurrency;

    /**
     * <ul>
     * <li>BG-20, BT-95: A coded identification of what VAT category applies to the document level allowance.</li>
     * <li>BG-20, BT-96: The VAT rate, represented as percentage that applies to the document level allowance.</li>
     * <li>KSA-21: Mandatory element. Use {@code VAT}</li>
     * </ul>
     */
    private TaxCategory taxCategory;
}
