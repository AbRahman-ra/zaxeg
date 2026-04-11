package sa.abrahman.zaxeg.adapter.generate.in.dto;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import jakarta.validation.Valid;
import lombok.Data;
import sa.abrahman.zaxeg.shared.contract.Mapable;
import sa.abrahman.zaxeg.adapter.generate.in.dto.components.*;
import sa.abrahman.zaxeg.core.generate.port.in.payload.*;

@Data
@NullMarked
public class InvoiceGenerationRequest implements Mapable<InvoiceGenerationPayload, Void> {

    @Valid
    private MetadataRequest metadata;

    @Valid
    private PartiesRequest parties;

    @Valid
    private LinesRequest lines;

    @Valid
    private CheckoutDetailsRequest checkout;

    @Override
    public InvoiceGenerationPayload mapped(Void d) {
        MetadataPayload metapay = metadata.mapped();
        PartiesPayload partiespay = parties.mapped();
        LinesPayload linespay = null; // lines.mapped(metapay.getInvoiceCurrency());
        CheckoutDetailsPayload checkoutpay = null; // checkout.mapped(List.of(metapay.getInvoiceCurrency(), metapay.getTaxCurrency()));

        return new InvoiceGenerationPayload(metapay, partiespay, linespay, checkoutpay);
    }
}
