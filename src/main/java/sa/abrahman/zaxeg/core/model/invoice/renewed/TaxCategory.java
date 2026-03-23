package sa.abrahman.zaxeg.core.model.invoice.renewed;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.party.TaxScheme;

@Getter
@Builder
public class TaxCategory {
    private VATCategory categoryCode;
    private TaxExemptionCode taxExemptionReasonCode;
    private String taxExemptionReason;
    private TaxScheme scheme;
}
