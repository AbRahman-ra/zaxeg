package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import sa.abrahman.zaxeg.core.model.Address;
import sa.abrahman.zaxeg.core.model.BusinessParty;
import sa.abrahman.zaxeg.core.model.Invoice;
import sa.abrahman.zaxeg.core.model.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.model.InvoiceLine;
import sa.abrahman.zaxeg.core.model.InvoiceSubtype;
import sa.abrahman.zaxeg.core.model.TaxCategory;

@Data
public class InvoiceRequest {

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Issue time is required")
    private LocalTime issueTime;

    @NotNull(message = "Invoice subtype is required")
    private InvoiceSubtype subtype;

    @NotNull(message = "Document type is required")
    private InvoiceDocumentType documentType;

    @Valid
    @NotNull(message = "Supplier information is required")
    private PartyRequest supplier;

    /**
     * Structurally optional. The Strategy layer will validate if it's mandatory
     * based on subtype.
     */
    @Valid
    private PartyRequest buyer;

    @Valid
    @NotEmpty(message = "At least one invoice line is required")
    private List<InvoiceLineRequest> lines;

    private BigDecimal prepaidAmount; // Optional

    public Invoice toDomainModel() {
        // lines
        List<InvoiceLine> domainLines = this.lines.stream()
                .map(line -> InvoiceLine.create(
                        line.getIdentifier(),
                        line.getName(),
                        line.getTaxCategory(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getLineDiscount() != null ? line.getLineDiscount() : BigDecimal.ZERO))
                .collect(Collectors.toList());

        // invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(this.invoiceNumber)
                .issueDate(this.issueDate)
                .issueTime(this.issueTime)
                .invoiceSubtype(this.subtype)
                .invoiceDocumentType(this.documentType)
                .supplier(this.supplier.toDomainModel())
                .buyer(this.buyer != null ? this.buyer.toDomainModel() : null)
                .lines(domainLines)
                .build();

        // calculate financials
        invoice.calculateFinancials(this.prepaidAmount);
        return invoice;
    }

    // ============ NESTED CLASSES ============
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

        @NotNull(message = "Unit price is required")
        @PositiveOrZero(message = "Unit price cannot be negative")
        private BigDecimal unitPrice;

        @PositiveOrZero(message = "Discount cannot be negative")
        private BigDecimal lineDiscount;

        private TaxCategory taxCategory;
    }
}
