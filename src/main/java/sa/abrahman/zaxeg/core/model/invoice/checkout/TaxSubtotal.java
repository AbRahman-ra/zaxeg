package sa.abrahman.zaxeg.core.model.invoice.checkout;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.common.Amount;
import sa.abrahman.zaxeg.core.model.invoice.common.TaxCategory;

@Getter
@Builder
public class TaxSubtotal {
    private Amount taxableAmount;
    private Amount taxAmount;
    private TaxCategory taxCategory;
}
