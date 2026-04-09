package sa.abrahman.zaxeg.adapter.generate.in.dto.components;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.shared.constant.rule.*;
import sa.abrahman.zaxeg.shared.contract.Mapable;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.MeasuringUnit;
import sa.abrahman.zaxeg.core.generate.port.in.payload.LinesPayload;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PayloadCommons;

@Getter
@AllArgsConstructor
@NullMarked
public class LinesRequest implements Mapable<LinesPayload, Currency> {
    @Valid
    @NotEmpty(message = UblRules.BR_16)
    @Schema(title = "Invoice Lines", requiredMode = RequiredMode.REQUIRED)
    private final List<InvoiceLine> invoiceLines;

    @Override
    public LinesPayload mapped(Currency data) {
        return new LinesPayload(
                this.invoiceLines != null ? invoiceLines.stream().map(l -> l.mapped(data)).toList() : List.of());
    }

    @Getter
    @NullMarked
    private static class InvoiceLine implements Mapable<LinesPayload.InvoiceLine, Currency> {

        @NotBlank(message = UblRules.BR_21)
        @Schema(title = "Invoice Line ID", description = "A unique identifier for the individual line within the Invoice. Usually a sequential number (1, 2, 3...).", requiredMode = RequiredMode.REQUIRED, example = "1")
        private String id;

        @Valid
        @NotNull(message = UblRules.BR_22)
        @Schema(title = "The invoice line quantity (unit, value)", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"unit\": \"PCE\", \"count\": 4.0\n}")
        private Quantity quantity;

        @NotNull(message = UblRules.BR_24)
        @Schema(title = "Invoice line net amount", description = "The total amount of the Invoice line, including allowances (discounts). It is the item net price multiplied with the quantity. The amount is “net” without VAT. Note: the currency must match the document level currency", requiredMode = RequiredMode.REQUIRED, example = "100.0")
        private BigDecimal netAmount;

        @Nullable
        @Schema(title = "Invoice line allowances or charges", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"value\": 100.0,\n    \"currency\":\"SAR\"\n}")
        private List<InvoiceGenerationRequestCommons.AllowanceOrCharge> allowanceCharges = List.of();

        @Valid
        @NotNull(message = ImplicitRules.LINE_VAT_AMOUNT_NOT_NULL)
        @Schema(title = "Total amounts", description = "contains taxAmount: VAT amount, and roundingAmount: Line amount invlusive VAT", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"taxAmount\": 0.15,\n    \"roundingAmount\": 1.15\n}")
        private InvoiceGenerationRequestCommons.TaxTotal vatLineAmount;

        @Valid
        @NotNull(message = ImplicitRules.LINE_ITEM_NOT_NULL)
        @Schema(title = "Invoice line item data (without prices)")
        private InvoiceLineItem item;

        @Valid
        @NotNull(message = ImplicitRules.LINE_PRICE_NOT_NULL)
        @Schema(title = "Invoice line item price details")
        private InvoiceLinePrice price;

        @Override
        public LinesPayload.InvoiceLine mapped(Currency data) {
            return LinesPayload.InvoiceLine.builder().id(id).quantity(quantity.mapped())
                    .netAmount(new PayloadCommons.Amount(netAmount, data)).vatLineAmount(vatLineAmount.mapped(data))
                    .item(item.mapped()).price(price.mapped(data)).build();
        }

        @Getter
        @AllArgsConstructor
        @NullMarked
        static class Quantity implements Mapable<LinesPayload.Quantity, Void> {
            @Schema(title = "Invoiced quantity unit of measure", description = "The unit of measure that applies to the invoiced quantity.", requiredMode = RequiredMode.REQUIRED, example = "PCE")
            private MeasuringUnit unit;

            @NotNull
            @Positive(message = KsaRules.BR_KSA_F_04)
            @Schema(title = "Invoiced quantity", description = "The quantity of items (goods or services) that is charged in the Invoice line.")
            private BigDecimal count;

            @Override
            public LinesPayload.Quantity mapped(Void d) {
                return new LinesPayload.Quantity(unit, count);
            }
        }

        @Getter
        @Builder
        @NullUnmarked
        static class InvoiceLineItem implements Mapable<LinesPayload.InvoiceLineItem, Void> {
            @NotBlank(message = UblRules.BR_25)
            @NonNull
            @Schema(title = "Item name", description = "The description of goods or services as per Article 53 of the VAT Implementing Regulation.")
            private String name;

            @Schema(title = "Item Buyer's Identifier", description = "An identifier, assigned by the Buyer, for the item.")
            private ItemPartyIdentifier itemBuyerIdentifier;

            @Schema(title = "Item Seller's Identifier", description = "An identifier, assigned by the Buyer, for the item.")
            private ItemPartyIdentifier itemSellerIdentifier;

            @Schema(title = "Item Standard Identifier", description = "An item identifier based on a registered scheme. This should include the product code type and the actual code. This list includes UPC (11 digit, 12 digit, 13 digit EAN), GTIN (14 digit), Customs HS Code and multiple other codes")
            private ItemPartyIdentifier itemStandardIdentifier;

            @NotNull(message = "Vat Information is required")
            @Schema(title = "VAT Information", description = "The VAT category code and rate for the invoiced item")
            private InvoiceGenerationRequestCommons.@NonNull TaxCategory classifiedTaxCategory;

            @Override
            public LinesPayload.InvoiceLineItem mapped(Void additionalData) {
                return LinesPayload.InvoiceLineItem.builder().name(name)
                        .itemBuyerIdentifier(
                                this.itemBuyerIdentifier != null ? this.itemBuyerIdentifier.mapped() : null)
                        .itemSellerIdentifier(
                                this.itemSellerIdentifier != null ? this.itemSellerIdentifier.mapped() : null)
                        .itemStandardIdentifier(
                                this.itemStandardIdentifier != null ? this.itemStandardIdentifier.mapped() : null)
                        .classifiedTaxCategory(classifiedTaxCategory.mapped()).build();

            }

            @Getter
            @AllArgsConstructor
            @NullMarked
            private static class ItemPartyIdentifier implements Mapable<LinesPayload.ItemPartyIdentifier, Void> {
                @NotBlank(message = ImplicitRules.ITEM_PARTY_ID_NOT_NULL)
                private String id;

                @Override
                public LinesPayload.ItemPartyIdentifier mapped(Void d) {
                    return new LinesPayload.ItemPartyIdentifier(id);
                }
            }
        }

        @Getter
        @Builder
        @NullUnmarked
        static class InvoiceLinePrice implements Mapable<LinesPayload.InvoiceLinePrice, Currency> {
            @NotNull(message = UblRules.BR_26)
            @Schema(title = "Item net price", description = "The price of an item, exclusive of VAT, after subtracting item price discount. The Item net price has to be equal with the Item gross price (allowance/charge amount) minus the Item price discount.")
            @NonNull
            private BigDecimal amount;

            @Schema(title = "Item price base quantity", description = "The number of item units to which the price applies.")
            private Quantity quantity;

            private InvoiceGenerationRequestCommons.AllowanceOrCharge allowanceOrCharge;

            @Override
            public LinesPayload.InvoiceLinePrice mapped(Currency currency) {
                return LinesPayload.InvoiceLinePrice.builder().amount(new PayloadCommons.Amount(amount, currency))
                        .quantity(quantity.mapped()).allowanceOrCharge(allowanceOrCharge.mapped(currency)).build();
            }
        }
    }
}
