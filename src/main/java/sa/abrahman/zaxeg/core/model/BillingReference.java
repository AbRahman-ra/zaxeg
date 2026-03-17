package sa.abrahman.zaxeg.core.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingReference {
    private String originalInvoiceNumber;
}
