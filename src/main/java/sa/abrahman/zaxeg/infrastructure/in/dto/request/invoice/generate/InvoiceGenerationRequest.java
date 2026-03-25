package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Data;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.*;
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
        MetadataPayload metap = metadata.toPayload(null);
        PartiesPayload partiesp = parties.toPayload(null);
        LinesPayload linesp = lines.toPayload(metap.getInvoiceCurrency());
        CheckoutDetailsPayload checkoutp = checkout.toPayload(List.of(metap.getInvoiceCurrency(), metap.getTaxCurrency()));

        return new InvoiceGenerationPayload(metap, partiesp, linesp, checkoutp);
    }
}
