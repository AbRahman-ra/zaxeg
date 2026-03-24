package sa.abrahman.zaxeg.core.model.invoice.common;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.checkout.TaxSubtotal;

@Getter
@Builder
public class TaxTotal {
    private Amount taxAmount;
    private Amount roundingAmount;
    private TaxSubtotal taxSubtotal;
}
