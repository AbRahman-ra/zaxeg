package sa.abrahman.zaxeg.core.model.invoice.renewed;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaxTotal {
    private Amount taxAmount;
    private Amount roundingAmount;
    private TaxSubtotal taxSubtotal;
}
