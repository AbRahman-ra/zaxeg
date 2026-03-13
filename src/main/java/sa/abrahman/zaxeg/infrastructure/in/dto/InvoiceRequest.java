package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import sa.abrahman.zaxeg.core.model.Invoice;

@Data
public class InvoiceRequest {
    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotBlank(message = "Buyer name is required")
    private String buyerName;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    public Invoice toDomainModel() {
        return Invoice.builder()
                .invoiceNumber(this.invoiceNumber)
                .buyerName(this.buyerName)
                .totalAmount(this.totalAmount)
                .build();
    }
}
