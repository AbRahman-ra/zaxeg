package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.math.BigDecimal;
import java.util.Currency;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Getter
@Builder
class TaxTotal implements Payloadable<InvoiceGenerationPayload.TaxTotal, Currency> {
    private BigDecimal taxAmount;
    private BigDecimal roundingAmount;
    private TaxSubtotal taxSubtotal;

    @Getter
    @Builder
    static class TaxSubtotal implements Payloadable<InvoiceGenerationPayload.TaxSubtotal, Currency> {
        private BigDecimal taxableAmount;
        private BigDecimal taxAmount;
        private TaxCategory taxCategory;

        @Override
        public InvoiceGenerationPayload.TaxSubtotal toPayload(Currency currency) {
            return InvoiceGenerationPayload.TaxSubtotal.builder()
                    .taxableAmount(new InvoiceGenerationPayload.Amount(taxableAmount, currency))
                    .taxAmount(new InvoiceGenerationPayload.Amount(taxAmount, currency))
                    .taxCategory(taxCategory == null ? null : taxCategory.toPayload(null))
                    .build();
        }
    }

    @Override
    public InvoiceGenerationPayload.TaxTotal toPayload(Currency currency) {
        return InvoiceGenerationPayload.TaxTotal.builder()
                .taxAmount(new InvoiceGenerationPayload.Amount(taxAmount, currency))
                .roundingAmount(new InvoiceGenerationPayload.Amount(roundingAmount, currency))
                .taxSubtotal(taxSubtotal == null ? null : taxSubtotal.toPayload(currency))
                .build();
    }
}
