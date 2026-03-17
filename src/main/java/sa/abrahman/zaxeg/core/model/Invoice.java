package sa.abrahman.zaxeg.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class Invoice {
    private static final String DEFAULT_CURRENCY = "SAR";

    // Metadata
    private String invoiceNumber;
    private UUID invoiceUuid;
    private LocalDate issueDate;
    private LocalTime issueTime;
    private LocalDate supplyDate;
    private InvoiceSubtype invoiceSubtype;
    private InvoiceDocumentType invoiceDocumentType;

    // Payments and billing
    private BillingReference billingReference;
    private String issuanceReason; // mandatory for credit / debit notes
    private PaymentMethod paymentMethod;

    @Builder.Default
    private Currency documentCurrency = Currency.getInstance(DEFAULT_CURRENCY);

    @Builder.Default
    private Currency taxCurrency = Currency.getInstance(DEFAULT_CURRENCY);

    // Phase 2 Cryptographic & Sequential Data
    @Setter private int invoiceCounterValue; // ICV
    @Setter private String previousInvoiceHash; // base64(PIH)
    @Setter private String cryptographicStamp; // base64(ECDSA Signature)
    @Setter private String generatedQrCode; // TLV base64

    // Business Entities
    private BusinessParty supplier;
    private BusinessParty buyer;

    // Line Items & Financials
    private List<InvoiceLine> lines;
    @Setter private DocumentFinancials financials;

    public void calculateFinancials(BigDecimal prepaidAmount) {
        if (this.lines == null || this.lines.isEmpty()) {
            throw new IllegalStateException("Cannot calculate financials without invoice lines.");
        }

        BigDecimal totalExtension = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (InvoiceLine line : this.lines) {
            totalExtension = totalExtension.add(line.getNetPrice());
            totalTax = totalTax.add(line.getTaxAmount());
        }

        BigDecimal totalInclusive = totalExtension.add(totalTax).setScale(2, RoundingMode.HALF_UP);
        BigDecimal safePrepaid = (prepaidAmount != null) ? prepaidAmount : BigDecimal.ZERO;
        BigDecimal payableAmount = totalInclusive.subtract(safePrepaid).setScale(2, RoundingMode.HALF_UP);

        this.financials = DocumentFinancials.builder()
                .totalLineExtensionAmount(totalExtension)
                .totalTaxAmount(totalTax)
                .totalAmountInclusive(totalInclusive)
                .prepaidAmount(safePrepaid)
                .payableAmount(payableAmount)
                .build();
    }
}
