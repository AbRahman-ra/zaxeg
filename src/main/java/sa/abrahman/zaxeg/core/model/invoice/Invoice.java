package sa.abrahman.zaxeg.core.model.invoice;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.wrapper.*;

/**
 * The main and most important Domain Model, obtained from <a href=
 * "https://zatca.gov.sa/ar/E-Invoicing/SystemsDevelopers/Documents/EInvoice_Data_Dictionary.xlsx">Electronic
 * Invoice Data Dictionary</a>, defining each invoice element and its meaning
 *
 * @implNote {@code UBL}: Universal Business Language
 * @implNote {@code BT}: Business Term
 * @implNote {@code BG}: Business Group
 */
@Getter
@Builder
public class Invoice {
    public static final String DEFAULT_CURRENCY = "SAR";

    /**
     * <p>Invoice Metadata, any invoice stateful information that is neither related to the items nor the amounts directlu will be stored here</p>
     *
     * <p><b>Example</b></p>
     * <ul>
     * <li>Invoice Document Subtype</li>
     * <li>Invoice Issue Date</li>
     * <li>Document Currecy</li>
     * <li>Tax Currency</li>
     * <li>etc...</li>
     * </ul>
     */
    private Metadata metadata;

    /**
     * Invoice Parties information, aka seller/supplier and buyer
     */
    private InvoiceParties parties;

    /**
     * Document Level Checkout information
     * <p><b>Example</b></p>
     * <ul>
     * <li>Document level totals</li>
     * <li>Document level charges and/or fees</li>
     * <li>Supplier IBAN (optional)</li>
     * <li>Payment method</li>
     * <li>etc...</li>
     * </ul>
     */
    private CheckoutDetails checkout;

    /**
     * Invocie lines information
     */
    private InvoiceLines lines;
}
