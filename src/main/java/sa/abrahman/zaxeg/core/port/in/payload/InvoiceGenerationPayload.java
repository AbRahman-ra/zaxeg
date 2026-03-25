package sa.abrahman.zaxeg.core.port.in.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvoiceGenerationPayload {
    private final MetadataPayload metadata;
    private final PartiesPayload parties;
    private final LinesPayload lines;
    private final CheckoutDetailsPayload checkout;
}
