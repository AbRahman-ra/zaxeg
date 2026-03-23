package sa.abrahman.zaxeg.core.model.invoice.renewed;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaxSubtotal {
    private Amount taxableAmount;
    private Amount taxAmount;
    private TaxCategory taxCategory;
}
