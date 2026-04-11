package sa.abrahman.zaxeg.core.generate.domain.model.invoice.wrapper;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.generate.domain.constant.PaymentMethod;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.checkout.LegalMonetaryTotals;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.common.AllowanceOrCharge;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.common.TaxTotal;

@Getter
@Builder
@NullMarked
public class CheckoutDetails {
    /**
     * BG16, BT-81: The means, expressed as code, for how a payment is expected to be or has been settled. Entries from
     * the UNTDID 4461 code list
     */
    private PaymentMethod paymentMeansType;

    /**
     * KSA-22: The payment terms, if mode of payment is credit. Free text
     */
    private String paymentTerms;

    /**
     * BG-17, BT-84:
     * <ul>
     * <li>The account number, IBAN, to which the transfer should be made.</li>
     * <li>In the case of factoring this account is owned by the factor.</li>
     * </ul>
     */
    private String paymentAccountIdentifier;

    /**
     * BG-20: An indicator that this AllowanceCharge describes a discount. The value of this tag indicating discount
     * (Allowance) must be {@code false}.
     */
    private List<AllowanceOrCharge> documentLevelAllowanceCharges;

    /**
     * BG-22: Invoice total amounts as mentioned in LegalMonetaryTotal XML tag
     */
    private LegalMonetaryTotals legalMonetaryTotals;

    /**
     * <ul>
     * <li>BG-22, BT-110:
     * <ul>
     * <li>The total VAT amount for the Invoice.</li>
     * <li>The Invoice total VAT amount is the sum of all VAT category tax amounts.</li>
     * </ul>
     * </li>
     * <li>BG-22, BT-05: Currency for total VAT amount</li>
     * </ul>
     *
     * @implSpec The {@code taxSubtotal} property is nullable here
     */
    private TaxTotal invoiceTaxTotals;

    /**
     * <h3>Tax Amount</h3>
     * <ul>
     * <li>BG-22, BT-111:
     * <ul>
     * <li>The VAT total amount expressed in the accounting currency accepted or required in the country of the
     * Seller.</li>
     * <li>To be used when the VAT accounting currency {@code taxCurrency} (BT-6) differs from the Invoice currency code
     * {@code documentCurrency} (BT-5).</li>
     * <li>The VAT amount in accounting currency is not used in the calculation of the Invoice totals.</li>
     * </ul>
     * </li>
     * <li>BG-22, BT-06: Currency for invoice total VAT amount in accounting currency</li>
     * </ul>
     * <h3>Tax Subtotal</h3>
     * <ul>
     * <li>BG-23, BT-116:
     * <ul>
     * <li>Sum of all taxable amounts subject to a specific VAT category code and VAT category rate (if the VAT category
     * rate is applicable).</li>
     * <li>The sum of Invoice line net amount minus allowances on document level which are subject to a specific VAT
     * category code and VAT category rate (if the VAT category rate is applicable).</li>
     * </ul>
     * </li>
     * <li>BG-23, BT-05: Currency for VAT category taxable amount</li>
     * <li>BG-23, BT-117: Calculated by multiplying the VAT category taxable amount with the VAT category rate for the
     * relevant VAT category.</li>
     * <li>BG-23, BT-05: Currency for category tax amount</li>
     * <li>BG-23, BT-118: Coded identification of a VAT category.</li>
     * <li>BG-23, BT-119: The VAT rate, represented as percentage that applies for the relevant VAT category.</li>
     * <li>BG-23, BT-121: A coded statement of the reason for why the amount is exempted from VAT.</li>
     * <li>BG-23, BT-120: A textual statement of the reason why the amount is exempted from VAT or why no VAT is being
     * charged</li>
     * <li>BG-23, KSA-21: Mandatory element. Use {@code VAT}</li>
     * </ul>
     *
     * @implNote The currency of this property should match {@code documentCurrency}
     * @implNote If the invoice is made in {@code SAR}, this property will have the same value as
     *           {@code invoiceTaxTotals}
     * @implSpec The {@code taxSubtotal} property is required here
     */
    private TaxTotal invoiceTaxTotalsInAccountingCurrency;
}
