package sa.abrahman.zaxeg.core.model.invoice.old.meta;

import lombok.Builder;
import lombok.Getter;

/**
 * @deprecated
 */
@Deprecated(forRemoval = true)
@Getter
@Builder
public class BillingReference {
    private String originalInvoiceNumber;
}
