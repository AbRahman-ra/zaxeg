package sa.abrahman.zaxeg.core.model.invoice.party;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartyTaxScheme {
    private String companyId;
    private TaxScheme taxScheme;
}
