package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import sa.abrahman.zaxeg.core.model.invoice.financial.*;
import sa.abrahman.zaxeg.core.model.invoice.meta.*;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.*;
import sa.abrahman.zaxeg.core.service.validator.rules.BusinessIntegrityConstraintRule;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
public class InvoiceRequest implements Payloadable<InvoiceGenerationPayload> {

    @Schema(description = "Invoice ID / Sequential Number", example = "INV0023")
    @NotBlank(message = BusinessIntegrityConstraintRule.BR_02)
    private String invoiceNumber;

    @Schema(description = "Optional; If not provided, the system will automatically generate a UUIDv4 for the invoice", example = "8f480da0-70f9-4674-8b63-8a3d6ebef9e6")
    private UUID invoiceUuid;

    @Schema(description = "Required; The date of invoice issuance (yyyy-MM-dd), cannot be in the future", example = "2026-01-01")
    @NotNull(message = BusinessIntegrityConstraintRule.BR_03)
    private LocalDate issueDate;

    @Schema(description = "Required; The time of invoice issuance (HH:mm:ss)", example = "19:48:21")
    @NotNull(message = "Issue time is required")
    private LocalTime issueTime;

    @Schema(description = "Optional; The date the supply was performed (yyyy-MM-dd)", example = "2026-01-05")
    private LocalDate supplyDate;

    @Schema(description = "Required; Determines the complexity of the invoice (`STANDARD` for B2B, `SIMPLIFIED` for B2C)", example = "STANDARD")
    @NotNull(message = "[INVOICE_SUBTYPE_ERROR]" + BusinessIntegrityConstraintRule.BR_04)
    private InvoiceSubtype subtype;

    @Schema(description = "Required; The type of document being issued (e.g., TAX_INVOICE, CREDIT_NOTE, DEBIT_NOTE)", example = "TAX_INVOICE")
    @NotNull(message = "[DOCUMENT_TYPE_ERROR]" + BusinessIntegrityConstraintRule.BR_04)
    private InvoiceDocumentType documentType;

    @Schema(description = "Required; 3-letter ISO 4217 Currency Code", example = "SAR")
    @NotNull(message = BusinessIntegrityConstraintRule.BR_05)
    private Currency documentCurrency;

    @Schema(description = "Required; Details of the supplier issuing the invoice")
    @Valid
    @NotNull(message = BusinessIntegrityConstraintRule.BR_06 + ", " +
            BusinessIntegrityConstraintRule.BR_08 + ", " +
            BusinessIntegrityConstraintRule.BR_09)
    private PartyRequest supplier;

    @Schema(description = "Conditional; Details of the buyer. Mandatory for Standard (B2B) invoices.")
    @Valid
    private PartyRequest buyer;

    @Schema(description = "Required; The goods or services being billed")
    @Valid
    @NotEmpty(message = BusinessIntegrityConstraintRule.BR_16)
    private List<InvoiceLineRequest> lines;

    @Schema(description = "Required; The means of payment (e.g., CASH, BANK_ACCOUNT, CREDIT_CARD)")
    @Valid
    @NotNull(message = BusinessIntegrityConstraintRule.BR_49)
    private PaymentMethod paymentMethod;

    @Schema(description = "Optional; Document-level discounts or charges applied to the total invoice amount")
    @Valid
    private List<AllowanceOrChargeRequest> discountsAndOrFees;

    @Schema(description = "Conditional; Mandatory if the document type is a CREDIT_NOTE or DEBIT_NOTE", example = "Customer returned damaged goods")
    private String issuanceReason;

    @Schema(description = "Conditional; Must be provided if issuing a Credit or Debit Note to link it to the original invoice")
    @Valid
    private BillingReferenceRequest billingReference;

    @Schema(description = "Optional; Provide pre-calculated financials to strictly validate against the engine's math to prevent rounding discrepancies, if not provided it will be automatically calulated and embedded in the invoice")
    @Valid
    private DocumentFinancialsRequest financials;

    @Override
    public InvoiceGenerationPayload toPayload() {
        // mapper
        Function<InvoiceLineRequest, LinePayload> domainLineMapper = l -> LinePayload.builder()
                .identifier(l.getIdentifier())
                .taxCategory(l.getTaxCategory())
                .name(l.getName())
                .exemptionReasonCode(l.getExemptionReasonCode())
                .exemptionReasonText(l.getExemptionReasonText())
                .quantity(l.getQuantity())
                .measuringUnit(l.getMeasuringUnit())
                .unitPrice(l.getUnitPrice())
                .netAmount(l.getNetAmount()) // if not provided calculated automatically
                .lineDiscount(l.getLineDiscount())
                .build();

        Function<AllowanceOrChargeRequest, InvoiceGlobalPayablePayload> allowanceChargeMapper = ac -> InvoiceGlobalPayablePayload
                .builder()
                .isCharge(!ac.isDiscount())
                .amount(ac.getAmount())
                .reason(ac.getReason())
                .taxCategory(ac.getTaxCategory())
                .exemptionReasonCode(ac.getVatExemptionReasonCode())
                .exemptionReasonText(ac.getVatExemptionReasonText())
                .build();

        // lines
        List<LinePayload> domainLines = this.lines.stream()
                .map(domainLineMapper)
                .toList();

        // allowance charges
        List<InvoiceGlobalPayablePayload> invoiceGlobalPayables = discountsAndOrFees == null
                ? List.of()
                : discountsAndOrFees.stream()
                        .map(allowanceChargeMapper)
                        .toList();

        BillingReferencePayload breference = Optional.ofNullable(billingReference)
                .map(BillingReferenceRequest::toPayload)
                .orElse(null);

        FinancialsPayload financialsRequest = Optional.ofNullable(this.financials)
                .map(DocumentFinancialsRequest::toPayload)
                .orElse(null);

        // assemble invoice
        return InvoiceGenerationPayload.builder()
                .invoiceNumber(this.invoiceNumber)
                .invoiceUuid(this.invoiceUuid)
                .issueDate(this.issueDate)
                .issueTime(this.issueTime)
                .supplyDate(this.supplyDate)
                .invoiceSubtype(this.subtype)
                .invoiceDocumentType(this.documentType)
                .documentCurrency(this.documentCurrency)
                .supplier(this.supplier.toPayload())
                .buyer(this.buyer != null ? this.buyer.toPayload() : null)
                .lines(domainLines)
                .paymentMethod(this.paymentMethod)
                .documentAllowancesAndOrCharges(invoiceGlobalPayables)
                .issuanceReason(this.issuanceReason)
                .billingReference(breference)
                .financials(financialsRequest)
                .build();
    }

    // ============ NESTED CLASSES ============
    @Data
    public static class BillingReferenceRequest implements Payloadable<BillingReferencePayload> {
        @NotBlank(message = "Original invoice number is mandatory for billing references")
        private String originalInvoiceNumber;

        @Override
        public BillingReferencePayload toPayload() {
            return BillingReferencePayload.builder()
                    .originalInvoiceNumber(this.originalInvoiceNumber)
                    .build();
        }
    }

    @Data
    public static class PartyRequest implements Payloadable<PartyPayload> {
        @NotBlank
        private String registrationName;
        private String vatNumber;
        private String commercialRegistrationNumber;
        @Valid
        private AddressRequest address;

        @Override
        public PartyPayload toPayload() {
            return PartyPayload.builder()
                    .registrationName(this.registrationName)
                    .vatNumber(this.vatNumber)
                    .commercialRegistrationNumber(this.commercialRegistrationNumber)
                    .address(this.address != null ? this.address.toPayload() : null)
                    .build();
        }
    }

    @Data
    public static class AddressRequest implements Payloadable<AddressPayload> {
        private String buildingNumber;
        private String streetName;
        private String district;
        private String city;
        private String postalCode;
        private String additionalNumber;
        private Locale country;

        @Override
        public AddressPayload toPayload() {
            return AddressPayload.builder()
                    .buildingNumber(this.buildingNumber)
                    .streetName(this.streetName)
                    .district(this.district)
                    .city(this.city)
                    .postalCode(this.postalCode)
                    .additionalNumber(this.additionalNumber)
                    .country(this.country)
                    .build();
        }
    }

    @Data
    public static class InvoiceLineRequest {

        @NotBlank(message = BusinessIntegrityConstraintRule.BR_21)
        private String identifier;

        @NotNull(message = BusinessIntegrityConstraintRule.BR_22)
        @Positive(message = "Quantity must be strictly greater than zero")
        private BigDecimal quantity;

        @NotBlank(message = BusinessIntegrityConstraintRule.BR_25)
        private String name;

        @NotNull(message = "Measuring Unit is required")
        private MeasuringUnit measuringUnit;

        @NotNull(message = BusinessIntegrityConstraintRule.BR_26)
        @PositiveOrZero(message = "Unit price cannot be negative")
        private BigDecimal unitPrice;

        @PositiveOrZero(message = "Discount cannot be negative")
        private BigDecimal lineDiscount;

        @Valid
        @NotNull(message = "BR-47: Each VAT breakdown shall be defined through a VAT category code")
        private TaxCategory taxCategory;

        private BigDecimal netAmount;
        private String exemptionReasonCode;
        private String exemptionReasonText;
    }

    @Data
    static class AllowanceOrChargeRequest {
        private boolean isDiscount; // is dicount is easier than isCharge for developers
        private String reason; // e.g., "VIP Discount" or "Delivery Fee"

        private BigDecimal amount; // The base amount of the discount/fee

        private BigDecimal tax; // The tax amount of the discount/fee

        @NotNull(message = "BR-47: Each VAT breakdown shall be defined through a VAT category code")
        private TaxCategory taxCategory; // The tax rate applied to this discount/fee

        /**
         * ZATCA strictly requires exemption reasons if the allowance/charge is
         * Zero-Rated or Exempt
         */
        private String vatExemptionReasonCode;
        private String vatExemptionReasonText;
    }

    @Data
    static class DocumentFinancialsRequest implements Payloadable<FinancialsPayload> {
        @NotNull(message = BusinessIntegrityConstraintRule.BR_13)
        private BigDecimal totalLineExtensionAmount;
        private BigDecimal totalTaxAmount;
        private BigDecimal totalTaxAmountInAccountingCurrency;

        @NotNull(message = BusinessIntegrityConstraintRule.BR_14)
        private BigDecimal totalAmountInclusive;
        private BigDecimal prepaidAmount;

        @NotNull(message = BusinessIntegrityConstraintRule.BR_15)
        private BigDecimal payableAmount;
        private BigDecimal taxExclusiveAmount;

        @Override
        public FinancialsPayload toPayload() {
            return FinancialsPayload.builder()
                    .totalLineExtensionAmount(totalLineExtensionAmount)
                    .totalTaxAmount(totalTaxAmount)
                    .totalTaxAmountInAccountingCurrency(totalTaxAmountInAccountingCurrency)
                    .totalAmountInclusive(totalAmountInclusive)
                    .prepaidAmount(prepaidAmount)
                    .payableAmount(payableAmount)
                    .build();
        }
    }
}
