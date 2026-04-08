package sa.abrahman.zaxeg.core.generate.domain.model.invoice.common;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaxTotal {
    private Amount taxAmount;
    private Amount roundingAmount;
    private List<TaxSubtotal> taxSubtotal;
}
