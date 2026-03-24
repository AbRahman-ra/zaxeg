package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.VATCategory;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Getter
@RequiredArgsConstructor
class TaxCategory implements Payloadable<InvoiceGenerationPayload.TaxCategory, Void> {
    private final VATCategory categoryCode;
    private final TaxExemptionCode taxExemptionReasonCode;
    private final String taxExemptionReason;
    private final TaxScheme scheme;

    @Override
    public InvoiceGenerationPayload.TaxCategory toPayload(Void additionalData) {
        return InvoiceGenerationPayload.TaxCategory.builder()
                .categoryCode(categoryCode)
                .taxExemptionReason(taxExemptionReason)
                .taxExemptionReasonCode(taxExemptionReasonCode)
                .scheme(scheme)
                .build();
    }
}
