package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.util.List;

import org.jspecify.annotations.NullMarked;

import jakarta.validation.Valid;
import lombok.Data;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.*;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
@NullMarked
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
        MetadataPayload metapay = metadata.toPayload();
        PartiesPayload partiespay = parties.toPayload();
        LinesPayload linespay = lines.toPayload(metapay.getInvoiceCurrency());
        CheckoutDetailsPayload checkoutpay = checkout.toPayload(List.of(metapay.getInvoiceCurrency(), metapay.getTaxCurrency()));

        return new InvoiceGenerationPayload(metapay, partiespay, linespay, checkoutpay);
    }
}
