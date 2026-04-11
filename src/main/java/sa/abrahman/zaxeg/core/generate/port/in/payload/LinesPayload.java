package sa.abrahman.zaxeg.core.generate.port.in.payload;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.generate.domain.constant.MeasuringUnit;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PayloadCommons.AllowanceOrCharge;

@Getter
@RequiredArgsConstructor
@NullMarked
public class LinesPayload {
    private final List<InvoiceLine> invoiceLines;

    @Data
    @Builder
    @NullMarked
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
    @NullMarked
    public static class InvoiceLineItem {
        private String name;

        @Nullable
        private ItemPartyIdentifier itemBuyerIdentifier;

        @Nullable
        private ItemPartyIdentifier itemSellerIdentifier;

        @Nullable
        private ItemPartyIdentifier itemStandardIdentifier;

        private PayloadCommons.TaxCategory classifiedTaxCategory;
    }

    @Getter
    @Builder
    @NullMarked
    public static class InvoiceLinePrice {
        private PayloadCommons.Amount amount;
        private Quantity quantity;

        @Nullable
        private AllowanceOrCharge allowanceOrCharge;
    }

    @Getter
    @RequiredArgsConstructor
    @NullMarked
    public static class ItemPartyIdentifier { private final String id; }

    @Getter
    @RequiredArgsConstructor
    @NullMarked
    public static class Quantity {
        private final MeasuringUnit unit;
        private final BigDecimal count;
    }
}
