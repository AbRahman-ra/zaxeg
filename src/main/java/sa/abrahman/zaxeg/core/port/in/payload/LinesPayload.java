package sa.abrahman.zaxeg.core.port.in.payload;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.MeasuringUnit;

@Getter
@RequiredArgsConstructor
public class LinesPayload {
    private final List<InvoiceLine> invoiceLines;

    @Data
    @Builder
    public static class InvoiceLine {
        private String id;
        private Quantity quantity;
        private PayloadCommons.Amount netAmount;
        private List<PayloadCommons.AllowanceOrCharge> allowanceCharges;
        private PayloadCommons.TaxTotal vatLineAmount;
        private InvoiceLineItem item;
        private InvoiceLinePrice price;

    }

    @Getter
    @Builder
    public static class InvoiceLineItem {
        private String name;
        private ItemPartyIdentifier itemBuyerIdentifier;
        private ItemPartyIdentifier itemSellerIdentifier;
        private ItemPartyIdentifier itemStandardIdentifier;
        private PayloadCommons.TaxCategory classifiedTaxCategory;
    }

    @Getter
    @Builder
    public static class InvoiceLinePrice {
        private PayloadCommons.Amount amount;
        private Quantity quantity;
        private PayloadCommons.AllowanceOrCharge allowanceOrCharge;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ItemPartyIdentifier {
        private final String id;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Quantity {
        private final MeasuringUnit unit;
        private final BigDecimal count;
    }
}
