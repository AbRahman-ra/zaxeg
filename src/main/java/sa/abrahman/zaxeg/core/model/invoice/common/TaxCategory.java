package sa.abrahman.zaxeg.core.model.invoice.common;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.VATCategory;

@Getter
@Builder
@NullMarked
public class TaxCategory {
    private VATCategory categoryCode;

    @Nullable
    private TaxExemptionCode taxExemptionReasonCode;

    @Nullable
    private String taxExemptionReason;

    private TaxScheme scheme;
}
