package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import org.jspecify.annotations.NullMarked;

import jakarta.validation.Valid;
import lombok.Data;
import sa.abrahman.zaxeg.core.port.in.payload.*;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;
import sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate.components.*;

@Data
@NullMarked
public class InvoiceGenerationRequest implements Payloadable<InvoiceGenerationPayload, Void> {

    @Valid
    private MetadataRequest metadata;

    @Valid
    private PartiesRequest parties;

    @Valid
    private LinesRequest lines;

    // @Valid
    // private CheckoutDetails checkout;

    @Override
    public InvoiceGenerationPayload toPayload(Void d) {
        MetadataPayload metapay = metadata.toPayload();
        PartiesPayload partiespay = parties.toPayload();
        // LinesPayload linespay = lines.toPayload(metapay.getInvoiceCurrency());
        // CheckoutDetailsPayload checkoutpay = checkout.toPayload(List.of(metapay.getInvoiceCurrency(), metapay.getTaxCurrency()));

        return new InvoiceGenerationPayload(metapay, partiespay, null, null);
    }
}
