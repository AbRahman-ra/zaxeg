package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.financial.*;
import sa.abrahman.zaxeg.core.model.invoice.meta.*;
import sa.abrahman.zaxeg.core.model.invoice.party.Address;
import sa.abrahman.zaxeg.core.model.invoice.party.BusinessParty;

@Data
public class InvoiceRequest {

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    private UUID invoiceUuid;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Issue time is required")
    private LocalTime issueTime;

    private LocalDate supplyDate;

    @NotNull(message = "Invoice subtype is required")
    private InvoiceSubtype subtype;

    @NotNull(message = "Document type is required")
    private InvoiceDocumentType documentType;

    @Valid
    private PaymentMethod paymentMethod;

    @Valid
    private List<AllowanceChargeRequest> discountsOrFees;

    /**
     * Validated in Domain if documentType == CREDIT_NOTE
     */
    private String issuanceReason;

    @Valid
    private BillingReferenceRequest billingReference;

    @Valid
    @NotNull(message = "Supplier information is required")
    private PartyRequest supplier;

    /**
     * Mandatory for standard notes.
     */
    @Valid
    private PartyRequest buyer;

    @Valid
    @NotEmpty(message = "At least one invoice line is required")
    private List<InvoiceLineRequest> lines;

    private BigDecimal prepaidAmount;

    public Invoice toDomainModel() {
        // mapper
        Function<InvoiceLineRequest, InvoiceLine> domainLineMapper = (l) -> InvoiceLine.create(
                l.getIdentifier(),
                l.getName(),
                l.getTaxCategory(),
                l.getExemptionReasonCode(),
                l.getExemptionReasonText(),
                l.getQuantity(),
                l.getMeasuringUnit(),
                l.getUnitPrice(),
                Optional.ofNullable(l.getLineDiscount()).orElse(BigDecimal.ZERO));
        Function<AllowanceChargeRequest, InvoiceGlobalPayable> allowanceChargeMapper = (ac) -> InvoiceGlobalPayable
                .builder()
                .isCharge(!ac.isDiscount())
                .amount(ac.getAmount())
                .reason(ac.getReason())
                .taxCategory(ac.getTaxCategory())
                .exemptionReasonCode(ac.getVatExemptionReasonCode())
                .exemptionReasonText(ac.getVatExemptionReasonText())
                .build();

        // lines
        List<InvoiceLine> domainLines = this.lines.stream()
                .map(domainLineMapper)
                .collect(Collectors.toList());

        // allowance charges
        List<InvoiceGlobalPayable> invoiceGlobalPayables = discountsOrFees == null
                ? null
                : discountsOrFees.stream()
                        .map(allowanceChargeMapper)
                        .collect(Collectors.toList());

        BillingReference breference = Optional.ofNullable(billingReference)
                .map(BillingReferenceRequest::toDomainModel)
                .orElse(null);

        // assemble invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(this.invoiceNumber)
                .issueDate(this.issueDate)
                .issueTime(this.issueTime)
                .supplyDate(this.supplyDate)
                .paymentMethod(this.paymentMethod)
                .invoiceSubtype(this.subtype)
                .invoiceDocumentType(this.documentType)
                .supplier(this.supplier.toDomainModel())
                .buyer(this.buyer != null ? this.buyer.toDomainModel() : null)
                .lines(domainLines)
                .billingReference(breference)
                .documentAllowanceCharges(invoiceGlobalPayables)
                .build();

        // calculate financials
        invoice.calculateFinancials(this.prepaidAmount);
        return invoice;
    }

    // ============ NESTED CLASSES ============
    @Data
    public static class BillingReferenceRequest {
        @NotBlank(message = "Original invoice number is mandatory for billing references")
        private String originalInvoiceNumber;

        public BillingReference toDomainModel() {
            return BillingReference.builder()
                    .originalInvoiceNumber(this.originalInvoiceNumber)
                    .build();
        }
    }

    @Data
    public static class PartyRequest {
        @NotBlank
        private String registrationName;
        private String vatNumber;
        private String commercialRegistrationNumber;
        @Valid
        private AddressRequest address;

        public BusinessParty toDomainModel() {
            return BusinessParty.builder()
                    .registrationName(this.registrationName)
                    .vatNumber(this.vatNumber)
                    .commercialRegistrationNumber(this.commercialRegistrationNumber)
                    .address(this.address != null ? this.address.toDomainModel() : null)
                    .build();
        }
    }

    @Data
    public static class AddressRequest {
        private String buildingNumber;
        private String streetName;
        private String district;
        private String city;
        private String postalCode;
        private String additionalNumber;

        public Address toDomainModel() {
            return Address.builder()
                    .buildingNumber(this.buildingNumber)
                    .streetName(this.streetName)
                    .district(this.district)
                    .city(this.city)
                    .postalCode(this.postalCode)
                    .additionalNumber(this.additionalNumber)
                    .build();
        }
    }

    @Data
    public static class InvoiceLineRequest {

        @NotBlank(message = "Line identifier is required")
        private String identifier;

        @NotBlank(message = "Line name is required")
        private String name;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be strictly greater than zero")
        private BigDecimal quantity;

        @NotNull(message = "Measuring Unit is required")
        private MeasuringUnit measuringUnit;

        @NotNull(message = "Unit price is required")
        @PositiveOrZero(message = "Unit price cannot be negative")
        private BigDecimal unitPrice;

        @PositiveOrZero(message = "Discount cannot be negative")
        private BigDecimal lineDiscount;

        private TaxCategory taxCategory;
        private String exemptionReasonCode;
        private String exemptionReasonText;
    }

    @Data
    static class AllowanceChargeRequest {
        private boolean isDiscount; // is dicount is easier than isCharge for developers
        private String reason; // e.g., "VIP Discount" or "Delivery Fee"
        private BigDecimal amount; // The base amount of the discount/fee
        private TaxCategory taxCategory; // The tax rate applied to this discount/fee

        /**
         * ZATCA strictly requires exemption reasons if the allowance/charge is
         * Zero-Rated or Exempt
         */
        private String vatExemptionReasonCode;
        private String vatExemptionReasonText;
    }
}
