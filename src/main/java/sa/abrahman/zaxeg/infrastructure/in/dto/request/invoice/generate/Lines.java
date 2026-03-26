package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.MeasuringUnit;
import sa.abrahman.zaxeg.core.port.in.payload.LinesPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Getter
@RequiredArgsConstructor
class Lines implements Payloadable<LinesPayload, Currency> {
    @Valid
    @NotEmpty(message = InvoiceValidationRule.BR_16)
    @Schema(title = "Invoice Lines", requiredMode = RequiredMode.REQUIRED)
    private final List<InvoiceLine> invoiceLines;

    @Override
    public LinesPayload toPayload(Currency data) {
        return new LinesPayload(invoiceLines.stream().map(l -> l.toPayload(data)).toList());
    }

    @Data
    private static class InvoiceLine implements Payloadable<LinesPayload.InvoiceLine, Currency> {

        @NotBlank(message = InvoiceValidationRule.BR_21)
        @Schema(title = "Invoice Line ID", description = "A unique identifier for the individual line within the Invoice. Usually a sequential number (1, 2, 3...).", requiredMode = RequiredMode.REQUIRED, example = "INV0023")
        private String id;

        @Valid
        @NotNull(message = InvoiceValidationRule.BR_22)
        @Positive(message = InvoiceValidationRule.BR_KSA_F_04)
        @Schema(title = "The invoice line quantity (unit, value)", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"unit\": \"PCE\", \"count\": 4.0\n}")
        private Quantity quantity;

        @Valid
        @NotNull(message = InvoiceValidationRule.BR_24)
        @Schema(title = "Invoice line net amount", description = "The total amount of the Invoice line, including allowances (discounts). It is the item net price multiplied with the quantity. The amount is “net” without VAT. Note: the currency must natch the document level currency", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"value\": 100.0,\n    \"currency\":\"SAR\"\n}")
        private BigDecimal netAmount;

        @Schema(title = "Invoice line net amount", description = "The total amount of the Invoice line, including allowances (discounts). It is the item net price multiplied with the quantity. The amount is “net” without VAT. Note: the currency must natch the document level currency", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"value\": 100.0,\n    \"currency\":\"SAR\"\n}")
        private List<Commons.AllowanceOrCharge> allowanceCharges = List.of();

        @Valid
        @Schema(title = "Total amounts", description = "VAT amount (taxAmount), Line amount invlusive VAT (roundingAmount)", requiredMode = RequiredMode.NOT_REQUIRED)
        private Commons.TaxTotal vatLineAmount;

        @Valid
        @Schema(title = "Invoice line item data (without prices)")
        private InvoiceLineItem item;

        @Valid
        private InvoiceLinePrice price;

        @Override
        public LinesPayload.InvoiceLine toPayload(Currency data) {
            return LinesPayload.InvoiceLine.builder()
                    .id(id)
                    .quantity(quantity.toPayload(null))
                    .netAmount(new PayloadCommons.Amount(netAmount, data))
                    .vatLineAmount(vatLineAmount.toPayload(data))
                    .item(item.toPayload(null))
                    .price(price.toPayload(data))
                    .build();
        }

        @Getter
        @RequiredArgsConstructor
        static class Quantity implements Payloadable<LinesPayload.Quantity, Void> {
            @Schema(title = "Invoiced quantity unit of measure", description = "The unit of measure that applies to the invoiced quantity.", requiredMode = RequiredMode.NOT_REQUIRED, example = "PCE")
            private MeasuringUnit unit;

            @NotNull
            @Schema(title = "Invoiced quantity", description = "The quantity of items (goods or services) that is charged in the Invoice line.")
            private BigDecimal count;

            @Override
            public LinesPayload.Quantity toPayload(Void d) {
                return new LinesPayload.Quantity(unit, count);
            }
        }

        @Getter
        @Builder
        static class InvoiceLineItem implements Payloadable<LinesPayload.InvoiceLineItem, Void> {
            @NotBlank(message = InvoiceValidationRule.BR_25)
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
            private Commons.TaxCategory classifiedTaxCategory;

            @Override
            public LinesPayload.InvoiceLineItem toPayload(Void additionalData) {
                return LinesPayload.InvoiceLineItem.builder()
                        .name(name)
                        .itemBuyerIdentifier(itemBuyerIdentifier.toPayload(null))
                        .itemSellerIdentifier(itemSellerIdentifier.toPayload(null))
                        .itemStandardIdentifier(itemStandardIdentifier.toPayload(null))
                        .classifiedTaxCategory(classifiedTaxCategory.toPayload(null))
                        .build();

            }

            @Getter
            @RequiredArgsConstructor
            private static class ItemPartyIdentifier implements Payloadable<LinesPayload.ItemPartyIdentifier, Void> {
                private final String id;

                @Override
                public LinesPayload.ItemPartyIdentifier toPayload(Void d) {
                    return new LinesPayload.ItemPartyIdentifier(id);
                }
            }
        }

        @Getter
        @Builder
        static class InvoiceLinePrice implements Payloadable<LinesPayload.InvoiceLinePrice, Currency> {
            @NotNull(message = InvoiceValidationRule.BR_26)
            @Schema(title = "Item net price", description = "The price of an item, exclusive of VAT, after subtracting item price discount. The Item net price has to be equal with the Item gross price (allowance/charge amount) minus the Item price discount.")
            private BigDecimal amount;

            @Schema(title = "Item price base quantity", description = "The number of item units to which the price applies.")
            private Quantity quantity;

            private Commons.AllowanceOrCharge allowanceOrCharge;

            @Override
            public LinesPayload.InvoiceLinePrice toPayload(Currency currency) {
                return LinesPayload.InvoiceLinePrice.builder()
                        .amount(new PayloadCommons.Amount(amount, currency))
                        .quantity(quantity.toPayload(null))
                        .allowanceOrCharge(allowanceOrCharge.toPayload(currency))
                        .build();
            }
        }
    }
}
