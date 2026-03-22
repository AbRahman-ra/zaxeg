package sa.abrahman.zaxeg.core.model.invoice.renewed;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaxCategory {
    /**
     * <p>BG-20, BT-95: A coded identification of what VAT category applies to the
     * document level allowance.</p>
     *
     * <p>BG-20, BT-96: The VAT rate, represented as percentage that applies to the
     * document level allowance.</p>
     */
    private VATCategory categoryCode;

    /**
     * Mandatory element. Use {@code VAT}
     */
    private Scheme scheme;
}
