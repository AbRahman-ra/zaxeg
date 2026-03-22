package sa.abrahman.zaxeg.core.model.invoice;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.financial.PaymentMethod;
import sa.abrahman.zaxeg.core.model.invoice.meta.*;
import sa.abrahman.zaxeg.core.model.invoice.renewed.*;

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
public class InvoiceOld {
    public static final String DEFAULT_CURRENCY = "SAR";

    // Metadata
    /** BT-01: A unique identification of the Invoice */
    private String invoiceNumber;

    /** KSA-01: Globally unique reference identifying the invoice. */
    @Builder.Default
    private UUID invoiceUuid = UUID.randomUUID();

    /**
     * BT-02: The date when the Invoice was issued as per Article 53 of the VAT
     * Implementing Regulation
     */
    private LocalDate invoiceIssueDate;

    /** KSA-25: The time when the invoice was issued. */
    private LocalTime invoiceIssueTime;

    /** BT-03: A code specifying the functional type of the Invoice. */
    private InvoiceDocumentType invoiceDocumentType;

    /** BT-03, KSA-02: A code of the invoice subtype and invoices transactions. */
    private InvoiceSubtype invoiceSubtype;

    /**
     * A textual note that gives unstructured information that is relevant to the
     * Invoice as a whole.
     */
    List<String> notes;

    /**
     * BT-05: The currency in which all Invoice amounts are given, except for the
     * Total VAT amount in accounting currency.
     */
    @Builder.Default
    private Currency invoiceCurrency = Currency.getInstance(DEFAULT_CURRENCY);

    /**
     * BT-06: The currency used for VAT accounting and reporting purposes as
     * accepted or required in the country of the Seller. Shall be used in
     * combination with the Total VAT amount in accounting currency (BT-111).
     */
    @Builder.Default
    private Currency taxCurrency = Currency.getInstance(DEFAULT_CURRENCY);

    /** BT-13: An identifier of a referenced purchase order, issued by the Buyer. */
    private DocumentReference purchaseOrder;

    /**
     * BG-3, BT-25: The sequential number (Invoice number BT-1) of the original
     * invoice(s) that the credit/debit note is related to.
     */
    private DocumentReference billingReference;

    /** BT-12: The identification of a contract. */
    private DocumentReference contract;

    /** KSA-16: Invoice counter value */
    private Long icv;

    /**
     * <p>
     * KSA-13: The base64 encoded SHA256 hash of the previous invoice. This hash
     * will be computed from the business payload of the previous invoice: UBL XML
     * of the previous invoice without tags for QR code (KSA-14) and cryptographic
     * stamp (KSA-15).
     * </p>
     *
     * <p>
     * For the first invoice, the previous invoice hash is
     * {@code "NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ=="},
     * the equivalent for base64 encoded SHA256 of {@code "0"} (zero) character.
     * </p>
     *
     * <p>
     * More details can be found in the Security Features Implementation Standard.
     * </p>
     */
    private String pih;

    /**
     * <p>
     * KSA-14: QR code must contain:
     * </p>
     *
     * <ul>
     * <li>Seller VAT number</li>
     * <li>Seller Name</li>
     * <li>VAT Total</li>
     * <li>Invoice Total (including VAT)</li>
     * <li>Hash of the XML invoice</li>
     * <li>Invoice issue date and time</li>
     * <li>QR Code stamp (For simplified tax invoices and associated notes, it will
     * be applied by the seller's device. For the tax invoice and associated notes,
     * it will be applied by ZATCA)</li>
     * </ul>
     *
     * <p>
     * More details can be found in the Security Features Implementation Standard.
     * </p>
     */
    private String qr;

    /**
     * <p>
     * KSA-15: Invoice cryptographic stamp
     * </p>
     *
     * <p>
     * More details can be found in the Security Features Implementation Standard.
     * </p>
     */
    private Object cryptographicStamp;

    /**
     * <p>
     * BT-29, BT-29-1: Other seller ID is one of the list:
     * </p>
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
    private List<PartyIdentification> otherSellerIds;

    /**
     * <ul>
     * <li>BG-05, BT-35: Seller address line 1 - the main address line in an
     * address</li>
     * <li>BG-05, BT-36: Seller address line 2 - an additional address line in an
     * address that can be used to give further details supplementing the main
     * line.</li>
     * <li>BG-05, KSA-17: Seller address building number</li>
     * <li>BG-05, KSA-23: Seller address additional number</li>
     * <li>BG-05, BT-37: The common name of the city, town or village, where the
     * Seller's address is located.</li>
     * <li>BG-05, BT-38: Seller post code</li>
     * <li>BG-05, BT-39: Seller country subdivision</li>
     * <li>BG-05, KSA-03: The name of the subdivision of the Seller city, town, or
     * village in which its address is located, such as the name of its district or
     * borough.</li>
     * <li>BG-05, BT-40: Seller country code</li>
     * <ul>
     */
    private PartyAddress sellerAddress;

    /**
     * <p>
     * BG-05, BT-31: Seller VAT identifier - taxpayer entity.
     * </p>
     *
     * <p>
     * Also known as Seller VAT identification number.
     * </p>
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
    private PartyIdentification otherBuyerId;

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
    private PartyAddress buyerAddress;

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
}
