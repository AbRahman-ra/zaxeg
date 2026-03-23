package sa.abrahman.zaxeg.core.model.invoice;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.old.financial.PaymentMethod;
import sa.abrahman.zaxeg.core.model.invoice.party.Address;
import sa.abrahman.zaxeg.core.model.invoice.party.PartyIdentification;
import sa.abrahman.zaxeg.core.model.invoice.party.PartyTaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.renewed.*;
import sa.abrahman.zaxeg.core.model.invoice.wrapper.InvoiceParties;
import sa.abrahman.zaxeg.core.model.invoice.wrapper.Metadata;

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
     * Invoice Parties information, aka Seller and buyer
     */
    private InvoiceParties parties;


    /**
     * <h3>Seller Name</h3>
     * <ul><li>BG-05, BT-27: Seller Name</li></ul>
     *
     * <h3>Seller Address</h3>
     * <ul>
     * <li>BG-05, BT-35: Seller address line 1 - the main address line in an address</li>
     * <li>BG-05, BT-36: Seller address line 2 - an additional address line in an address that can be used to give further details supplementing the main line.</li>
     * <li>BG-05, KSA-17: Seller address building number</li>
     * <li>BG-05, KSA-23: Seller address additional number</li>
     * <li>BG-05, BT-37: The common name of the city, town or village, where the Seller's address is located.</li>
     * <li>BG-05, BT-38: Seller post code</li>
     * <li>BG-05, BT-39: Seller country subdivision</li>
     * <li>BG-05, KSA-03: The name of the subdivision of the Seller city, town, or village in which its address is located, such as the name of its district or borough.</li>
     * <li>BG-05, BT-40: Seller country code</li>
     * </ul>
     *
     * <h3>Seller VAT Identification</h3>
     * <ul>
     * <li>
     * BG-05, BT-31:
     * <ul>
     * <li>Seller VAT identifier - taxpayer entity.</li>
     * <li>Also known as Seller VAT identification number.</li>
     * </ul>
     * </li>
     * </ul>
    *
    *
     * <h3>Other seller IDs</h3>
     * BT-29, BT-29-1: Other seller ID is one of the list:
     *
     * <ol>
     * <li>Commercial registration number with "CRN" as schemeID</li>
     * <li>Momra license with "MOM" as schemeID</li>
     * <li>MLSD license with "MLS" as schemeID</li>
     * <li>Sagia license with "SAG" as schemeID</li>
     * <li>Other OD with "OTH" as schemeID</li>
     * </ol>
     *
     * <p>
     * In case multiple IDs exist then one of the above must be entered following
     * the sequence specified above
     * </p>
     */
    private Address sellerAddress;

    /**

     */
    private PartyTaxScheme sellerVatIdentification;

    /** BG-05, BT-27: Seller Name */
    private String sellerName;

    /**
     * <p>
     * BG-07, BT-46, BT-46-1: Other Buyer ID must be one of the following list:
     * </p>
     * <ul>
     * <li>Tax Identification Number "TIN" as schemeID</li>
     * <li>Commercial registration number with "CRN" as schemeID</li>
     * <li>Momra license with "MOM" as schemeID</li>
     * <li>MLSD license with "MLS" as schemeID</li>
     * <li>700 Number with "700" as schemeID</li>
     * <li>Sagia license with "SAG" as schemeID</li>
     * <li>National ID with "NAT" as schemeID</li>
     * <li>GCC ID with "GCC" as schemeID</li>
     * <li>Iqama Number with "IQA" as schemeID</li>
     * <li>Passport ID with "PAS" as schemeID</li>
     * <li>Other ID with "OTH" as schemeID</li>
     * </ul>
     *
     * <p>
     * In case multiple IDs exist then one of the above must be entered following
     * the sequence specified above
     * </p>
     */
    private List<PartyIdentification> otherBuyerIds;

    /**
     * <ul>
     * <li>BG-08, BT-50: Buyer address line 1 - the main address line in an
     * address</li>
     * <li>BG-08, BT-51: Buyer address line 2 - an additional address line in an
     * address that can be used to give further details supplementing the main
     * line.</li>
     * <li>BG-08, KSA-18: Buyer address building number</li>
     * <li>BG-08, KSA-19: Buyer address additional number</li>
     * <li>BG-08, BT-52: The common name of the city, town or village, where the
     * Buyer's address is located.</li>
     * <li>BG-08, BT-53: Buyer post code</li>
     * <li>BG-08, BT-54: Buyer country subdivision</li>
     * <li>BG-08, KSA-04: The name of the subdivision of the Buyer city, town, or
     * village in which its address is located, such as the name of its district or
     * borough.</li>
     * <li>BG-08, BT-55: Buyer country code</li>
     * <ul>
     */
    private Address buyerAddress;

    /**
     * BG-07, BT-48, The Buyer's VAT identifier (also known as Buyer VAT
     * identification number).
     */
    private PartyTaxScheme buyerVatIdentification;

    /**
     * BG-07, BT-44: The full name of the Buyer.
     */
    private String buyerName;

    /**
     * <p>
     * BG-13, KSA-05: The date when the supply is performed
     * </p>
     * <p>
     * For credit and debit notes , it acts as the original supply date.
     * </p>
     */
    private LocalDate supplyDate;

    /**
     * KSA-24: Calendar field "End Date" for Continuous Supplies.
     */
    private LocalDate supplyEndDate;

    /**
     * BG16, BT-81: The means, expressed as code, for how a payment is expected to
     * be or has been settled. Entries from the UNTDID 4461 code list
     */
    private PaymentMethod paymentMeansType;

    /**
     * <p>
     * KSA-10: Reasons for issuance of credit / debit note as per Article 40
     * (paragraph 1) and Article 54 (3) of KSA VAT regulations, a Credit and Debit
     * Note is issued for these 5 instances:
     * </p>
     *
     * <ul>
     * <li>Cancellation or suspension of the supplies after its occurrence either
     * wholly or partially</li>
     * <li>In case of essential change or amendment in the supply, which leads to
     * the change of the VAT due;</li>
     * <li>Amendment of the supply value which is pre-agreed upon between the
     * supplier and consumer;</li>
     * <li>In case of goods or services refund.</li>
     * <li>In case of change in Seller's or Buyer's information</li>
     * </ul>
     */
    private List<String> creditOrDebitNoteReasons;

    /**
     * KSA-22: The payment terms, if mode of payment is credit. Free text
     */
    private String paymentTerms;

    /**
     * BG-17, BT-84: The account number, IBAN, to which the transfer should be made.
     * In the case of factoring this account is owned by the factor.
     */
    private String paymentAccountIdentifier;

    /**
     * BG-20: An indicator that this AllowanceCharge describes a discount. The value
     * of this tag indicating discount (Allowance) must be {@code false}.
     */
    private List<AllowanceOrCharge> documentLevelAllowanceCharges;

    /**
     * BG-22: Invoice total amounts as mentioned in LegalMonetaryTotal XML tag
     */
    private LegalMonetaryTotals documentTotals;

    /**
     * <ul>
     * <li>BG-22, BT-110:
     * <ul>
     * <li>The total VAT amount for the Invoice.</li>
     * <li>The Invoice total VAT amount is the sum of all VAT category tax
     * amounts.</li>
     * </ul>
     * </li>
     *
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
     * <li>The VAT total amount expressed in the accounting currency accepted or
     * required in the country of the Seller.</li>
     * <li>To be used when the VAT accounting currency {@code taxCurrency} (BT-6)
     * differs from the Invoice currency code {@code documentCurrency} (BT-5).</li>
     * <li>The VAT amount in accounting currency is not used in the calculation of
     * the Invoice totals.</li>
     * </ul>
     * </li>
     *
     * <li>BG-22, BT-06: Currency for invoice total VAT amount in accounting
     * currency</li>
     * </ul>
     *
     * <h3>Tax Subtotal</h3>
     * <ul>
     * <li>BG-23, BT-116:
     * <ul>
     * <li>Sum of all taxable amounts subject to a specific VAT category code and
     * VAT category rate (if the VAT category rate is applicable).</li>
     * <li>The sum of Invoice line net amount minus allowances on document level
     * which are subject to a specific VAT category code and VAT category rate (if
     * the VAT category rate is applicable).</li>
     * </ul>
     * </li>
     *
     * <li>BG-23, BT-05: Currency for VAT category taxable amount</li>
     *
     * <li>BG-23, BT-117: Calculated by multiplying the VAT category taxable amount
     * with the VAT category rate for the relevant VAT category.</li>
     * <li>BG-23, BT-05: Currency for category tax amount</li>
     * <li>BG-23, BT-118: Coded identification of a VAT category.</li>
     * <li>BG-23, BT-119: The VAT rate, represented as percentage that applies for
     * the relevant VAT category.</li>
     * <li>BG-23, BT-121: A coded statement of the reason for why the amount is
     * exempted from VAT.</li>
     * <li>BG-23, BT-120: A textual statement of the reason why the amount is
     * exempted from VAT or why no VAT is being charged</li>
     * <li>BG-23, KSA-21: Mandatory element. Use {@code VAT}</li>
     * </ul>
     *
     * @implNote The currency of this property should match {@code documentCurrency}
     * @implNote If the invoice is made in {@code SAR}, this property will have the
     *           same value as {@code invoiceTaxTotals}
     * @implSpec The {@code taxSubtotal} property is required here
     */
    private TaxTotal invoiceTaxTotalsInAccountingCurrency;

    private List<InvoiceLine> lines;
}
