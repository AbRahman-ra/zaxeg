package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
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

    @NotBlank(message = "BR-02: An Invoice shall have an Invoice number")
    private String invoiceNumber;

    private UUID invoiceUuid;

    @NotNull(message = "BR-03: An Invoice shall have an Invoice issue date")
    private LocalDate issueDate;

    @NotNull(message = "Issue time is required")
    private LocalTime issueTime;

    private LocalDate supplyDate;

    @NotNull(message = "[INVOICE_SUBTYPE_ERROR] BR-04: An Invoice shall have an Invoice type code")
    private InvoiceSubtype subtype;

    @NotNull(message = "[DOCUMENT_TYPE_ERROR] BR-04: An Invoice shall have an Invoice type code")
    private InvoiceDocumentType documentType;

    @NotNull(message = "BR-05: An Invoice shall have an Invoice currency code")
    private Currency documentCurrency;

    @Valid
    @NotNull(message = """
            BR-06: An Invoice shall contain the Seller name,
            BR-08: An Invoice shall contain the Seller postal address,
            BR-09: The Seller postal address shall contain a Seller country code
            """)
    private PartyRequest supplier;

     /**
     * Mandatory for standard notes.
     */
    @Valid
    private PartyRequest buyer;

    @Valid
    @NotEmpty(message = "BR-16: An Invoice must have at least one line item")
    private List<InvoiceLineRequest> lines;

    @Valid
    @NotNull(message = "BR-49: A Payment instruction shall specify the Payment means type code")
    private PaymentMethod paymentMethod;

    @Valid
    private List<AllowanceChargeRequest> discountsOrFees;

    /**
     * Validated in Domain if documentType == CREDIT_NOTE
     */
    private String issuanceReason;

    @Valid
    private BillingReferenceRequest billingReference;

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

        @NotBlank(message = "BR-21: Each Invoice line shall have an Invoice line identifier")
        private String identifier;

        @NotBlank(message = "BR-25: Each Invoice line shall contain the Item name")
        private String name;

        @NotNull(message = "BR-22: Each Invoice line shall have an Invoiced quantity")
        @Positive(message = "Quantity must be strictly greater than zero")
        private BigDecimal quantity;

        @NotNull(message = "Measuring Unit is required")
        private MeasuringUnit measuringUnit;

        @NotNull(message = "BR-24: Each Invoice line shall have an Invoiced line net amount")
        @PositiveOrZero(message = "Unit price cannot be negative")
        private BigDecimal unitPrice;

        @PositiveOrZero(message = "Discount cannot be negative")
        private BigDecimal lineDiscount;

        @Valid
        @NotNull(message = "BR-47: Each VAT breakdown shall be defined through a VAT category code")
        private TaxCategory taxCategory;
        private String exemptionReasonCode;
        private String exemptionReasonText;
    }

    @Data
    static class AllowanceChargeRequest {
        private boolean isDiscount; // is dicount is easier than isCharge for developers
        private String reason; // e.g., "VIP Discount" or "Delivery Fee"

        @NotNull(message = "BR-41/BR-43: Each Invoice line allowance/charge shall have an Invoice line allowance/charge amount")
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
}
