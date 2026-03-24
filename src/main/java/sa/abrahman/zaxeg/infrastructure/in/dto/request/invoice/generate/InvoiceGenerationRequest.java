package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import jakarta.validation.Valid;
import lombok.Data;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
public class InvoiceGenerationRequest implements Payloadable<InvoiceGenerationPayload, Void> {

    @Valid
    private Metadata metadata;

    @Valid
    private Parties parties;

    @Valid
    private Lines lines;

    @Valid
    private CheckoutDetails checkout;

    @Override
    public InvoiceGenerationPayload toPayload(Void d) {
        InvoiceGenerationPayload.Metadata meta = metadata.toPayload(null);
        return new InvoiceGenerationPayload(meta, parties.toPayload(null), lines.toPayload(meta.getInvoiceCurrency()));
    }

    @Data
    static class CheckoutDetails {
    }

}
