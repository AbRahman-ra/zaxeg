package sa.abrahman.zaxeg.core.generate.port.in.payload;

import org.jspecify.annotations.NullMarked;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@NullMarked
public class InvoiceGenerationPayload {
    private final MetadataPayload metadata;
    private final PartiesPayload parties;
    private final LinesPayload lines;
    private final CheckoutDetailsPayload checkout;
}
