package sa.abrahman.zaxeg.core.model.invoice;

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
import sa.abrahman.zaxeg.core.model.invoice.meta.*;
import sa.abrahman.zaxeg.core.model.invoice.party.*;
import sa.abrahman.zaxeg.core.model.invoice.financial.*;

@Getter
@Builder
public class Invoice {
    private static final String DEFAULT_CURRENCY = "SAR";

    // Metadata
    private String invoiceNumber;
    @Builder.Default
    private UUID invoiceUuid = UUID.randomUUID();
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
    private List<InvoiceGlobalPayable> documentAllowanceCharges;

    public void calculateFinancials(BigDecimal prepaidAmount) {
        if (this.lines == null || this.lines.isEmpty()) {
            throw new IllegalStateException("Cannot calculate financials without invoice lines.");
        }

        BigDecimal totalExtension = BigDecimal.ZERO;
        BigDecimal totalLineTax = BigDecimal.ZERO;

        // Add lines
        for (InvoiceLine line : this.lines) {
            totalExtension = totalExtension.add(line.getNetPrice());
            totalLineTax = totalLineTax.add(line.getTaxAmount());
        }

        // document allowances and charges
        BigDecimal totalDocAllowances = BigDecimal.ZERO;
        BigDecimal totalDocCharges = BigDecimal.ZERO;
        BigDecimal totalDocAllowanceTax = BigDecimal.ZERO;
        BigDecimal totalDocChargeTax = BigDecimal.ZERO;

        if (this.documentAllowanceCharges != null) {
            for (InvoiceGlobalPayable ac : this.documentAllowanceCharges) {
                if (ac.isCharge()) {
                    totalDocCharges = totalDocCharges.add(ac.getAmount());
                    totalDocChargeTax = totalDocChargeTax.add(ac.getTaxAmount());
                } else {
                    totalDocAllowances = totalDocAllowances.add(ac.getAmount());
                    totalDocAllowanceTax = totalDocAllowanceTax.add(ac.getTaxAmount());
                }
            }
        }

        // Tax Exclusive = Lines - Allowances + Charges
        BigDecimal taxExclusiveAmount = totalExtension.subtract(totalDocAllowances).add(totalDocCharges);

        // Total Tax = Line Taxes - Allowance Taxes + Charge Taxes
        BigDecimal totalTaxAmount = totalLineTax.subtract(totalDocAllowanceTax).add(totalDocChargeTax);

        // Total Inclusive = Exclusive + Tax
        BigDecimal totalInclusive = taxExclusiveAmount.add(totalTaxAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal safePrepaid = (prepaidAmount != null) ? prepaidAmount : BigDecimal.ZERO;
        BigDecimal payableAmount = totalInclusive.subtract(safePrepaid).setScale(2, RoundingMode.HALF_UP);

        this.financials = DocumentFinancials.builder()
                .totalLineExtensionAmount(totalExtension)
                .taxExclusiveAmount(taxExclusiveAmount)
                .totalTaxAmount(totalTaxAmount)
                .totalAmountInclusive(totalInclusive)
                .prepaidAmount(safePrepaid)
                .payableAmount(payableAmount)
                .build();
    }
}
