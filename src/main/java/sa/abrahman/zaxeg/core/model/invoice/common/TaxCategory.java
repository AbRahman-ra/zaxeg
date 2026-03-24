package sa.abrahman.zaxeg.core.model.invoice.common;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.VATCategory;

@Getter
@Builder
public class TaxCategory {
    private VATCategory categoryCode;
    private TaxExemptionCode taxExemptionReasonCode;
    private String taxExemptionReason;
    private TaxScheme scheme;
}
