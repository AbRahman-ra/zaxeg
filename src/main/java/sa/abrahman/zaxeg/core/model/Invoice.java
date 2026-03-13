package sa.abrahman.zaxeg.core.model;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class Invoice {
    private String invoiceNumber;
    private String buyerName;
    private BigDecimal totalAmount;

    @Setter private UUID invoiceUuid;
    @Setter private int invoiceCounterValue; 
    @Setter private String previousInvoiceHash;
}
