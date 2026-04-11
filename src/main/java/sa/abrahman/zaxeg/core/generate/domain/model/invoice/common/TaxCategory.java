package sa.abrahman.zaxeg.core.generate.domain.model.invoice.common;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.generate.domain.constant.TaxExemptionCode;
import sa.abrahman.zaxeg.core.generate.domain.constant.TaxScheme;
import sa.abrahman.zaxeg.core.generate.domain.constant.VatCategory;

@Getter
@Builder
@NullMarked
public class TaxCategory {
    private VatCategory categoryCode;

    @Nullable
    private TaxExemptionCode taxExemptionReasonCode;

    @Nullable
    private String taxExemptionReason;

    private TaxScheme scheme;
}
