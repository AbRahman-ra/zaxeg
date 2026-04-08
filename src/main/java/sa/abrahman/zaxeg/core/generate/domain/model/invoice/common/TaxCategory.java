package sa.abrahman.zaxeg.core.generate.domain.model.invoice.common;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.TaxExemptionCode;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.TaxScheme;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.VATCategory;

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
