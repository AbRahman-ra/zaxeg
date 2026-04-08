package sa.abrahman.zaxeg.core.generate.domain.model.invoice.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.TaxScheme;

@Getter
@RequiredArgsConstructor
public class PartyTaxScheme {
    private final String companyId;
    private final TaxScheme taxScheme;

    public static PartyTaxScheme of(String companyId, TaxScheme taxScheme) {
        return new PartyTaxScheme(companyId, taxScheme);
    }

    /**
     * Creates a new {@code PartyTaxScheme} using {@code VAT} as the default tax scheme
     * 
     * @param companyId
     * @return new instance
     */
    public static PartyTaxScheme of(String companyId) {
        return new PartyTaxScheme(companyId, TaxScheme.VAT);
    }
}
