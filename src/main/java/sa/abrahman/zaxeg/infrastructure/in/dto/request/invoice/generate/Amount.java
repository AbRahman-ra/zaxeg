package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Getter
@RequiredArgsConstructor
class Amount implements Payloadable<InvoiceGenerationPayload.Amount, Void> {
    private final BigDecimal value;
    private final Currency currency;

    @Override
    public InvoiceGenerationPayload.Amount toPayload(Void d) {
        return new InvoiceGenerationPayload.Amount(value, currency);
    }
}
