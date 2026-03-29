package sa.abrahman.zaxeg.core.model.invoice.wrapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.metadata.*;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType;

/**
 * <p>
 * Invoice Metadata, any invoice stateful information that is neither related to
 * the items nor the amounts directlu will be stored here
 * </p>
 *
 * <p>
 * <b>Example</b>
 * </p>
 * <ul>
 * <li>Invoice Document Subtype</li>
 * <li>Invoice Issue Date</li>
 * <li>Document Currecy</li>
 * <li>Tax Currency</li>
 * <li>etc...</li>
 * </ul>
 */
@Getter
@Builder(toBuilder = true)
@NullMarked
public class Metadata {
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
    private InvoiceTypeTransactions invoiceTypeTransactions;

    /**
     * BG-01, BT-22: A textual note that gives unstructured information that is
     * relevant to the Invoice as a whole.
     */
    private List<String> notes;

    /**
     * BT-05: The currency in which all Invoice amounts are given, except for the
     * Total VAT amount in accounting currency.
     */
    @Builder.Default
    private Currency invoiceCurrency = Currency.getInstance(Invoice.DEFAULT_LOCALE_CODE);

    /**
     * BT-06: The currency used for VAT accounting and reporting purposes as
     * accepted or required in the country of the Seller. Shall be used in
     * combination with the Total VAT amount in accounting currency (BT-111).
     */
    @Builder.Default
    private Currency taxCurrency = Currency.getInstance(Invoice.DEFAULT_LOCALE_CODE);

    /** BT-13: An identifier of a referenced purchase order, issued by the Buyer. */
    @Nullable
    private DocumentReference purchaseOrder;

    /**
     * BG-3, BT-25: The sequential number (Invoice number BT-1) of the original
     * invoice(s) that the credit/debit note is related to.
    */
    @Nullable
    private DocumentReference billingReference;

    /** BT-12: The identification of a contract. */
    @Nullable
    private DocumentReference contract;

    /** KSA-16: Invoice counter value */
    private Long icv;

    /**
     * <ul>
     * <li>KSA-13:
     * <ul>
     * <li>The base64 encoded SHA256 hash of the previous invoice. This hash will be
     * computed from the business payload of the previous invoice: UBL XML of the
     * previous invoice without tags for QR code (KSA-14) and cryptographic stamp
     * (KSA-15).</li>
     * <li>For the first invoice, the previous invoice hash is
     * {@code "NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ=="},
     * the equivalent for base64 encoded SHA256 of {@code "0"} (zero)
     * character.</li>
     * </ul>
     *
     * <p>
     * More details can be found in the Security Features Implementation Standard.
     * </p>
     */
    private String pih;

    /**
     * KSA-14: QR code must contain:
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
     * KSA-15: Invoice cryptographic stamp
     *
     * <p>
     * More details can be found in the Security Features Implementation Standard.
     * </p>
     */
    private CryptographicStamp cryptographicStamp;

    /**
     * BG-13, KSA-05:
     * <ul>
     * <li>The date when the supply is performed</li>
     * <li>For credit and debit notes , it acts as the original supply date.</li>
     * </ul>
     */
    @Nullable
    private LocalDate supplyDate;

    /**
     * KSA-24: Calendar field "End Date" for Continuous Supplies.
    */
    @Nullable
    private LocalDate supplyEndDate;

    /**
     * <ul>
     * <li>KSA-10: Reasons for issuance of credit / debit note as per Article 40
     * (paragraph 1) and Article 54 (3) of KSA VAT regulations, a Credit and Debit
     * Note is issued for these 5 instances:</li>
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
    * </ul>
    */
    private List<String> creditOrDebitNoteIssuanceReasons;
}
