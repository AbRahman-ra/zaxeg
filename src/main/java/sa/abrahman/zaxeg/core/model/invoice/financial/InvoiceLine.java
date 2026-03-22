package sa.abrahman.zaxeg.core.model.invoice.financial;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class InvoiceLine {
    // metadata
    /**
     * Example: SKU-12345
     */
    private String identifier;
    /**
     * Example: Legal Consultiation
     */
    private String name;

    @Builder.Default
    private TaxCategory taxCategory = TaxCategory.STANDARD;
    // We will use setter methods when instantiating the specific exemption reason
    /**
     * TODO: make it enum
     */
    @Setter private String exemptionReasonCode; // e.g., "VATEX-SA-32"
    @Setter private String exemptionReasonText; // e.g., "Export of goods"

    // Base Values
    private BigDecimal quantity;

    @Builder.Default
    private MeasuringUnit measuringUnit = MeasuringUnit.PCE;

    /**
     * Price strictly BEFORE tax and discounts
     */
    private BigDecimal unitPrice;

    /**
     * Discount applied to this specific line
     */
    private BigDecimal lineDiscount;

    // Calculated Values (Validated by ZATCA BR-KSA-51 & BR-KSA-52)
    /**
     * (quantity * unitPrice) - lineDiscount
     */
    private BigDecimal netPrice;

    /**
     * netPrice * (taxCategory.rate / 100)
     */
    private BigDecimal taxAmount;

    /**
     * netPrice + taxAmount
     */
    private BigDecimal lineTotalInclusive;

    /**
     * @deprecated
     * @param identifier
     * @param name
     * @param taxCategory
     * @param exemptionReasonCode
     * @param exemptionReasonText
     * @param quantity
     * @param measuringUnit
     * @param unitPrice
     * @param lineDiscount
     * @return
     */
    @Deprecated(forRemoval = true)
    public static InvoiceLine create(
            String identifier,
            String name,
            TaxCategory taxCategory,
            String exemptionReasonCode,
            String exemptionReasonText,
            BigDecimal quantity,
            MeasuringUnit measuringUnit,
            BigDecimal unitPrice,
            BigDecimal lineDiscount) {
        if (taxCategory == null) taxCategory = TaxCategory.STANDARD;

        BigDecimal rawNetPrice = quantity.multiply(unitPrice).subtract(lineDiscount);
        BigDecimal netPrice = rawNetPrice.setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxMultiplier = taxCategory.getRate().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = netPrice.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);

        BigDecimal lineTotalInclusive = netPrice.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        MeasuringUnit munit = measuringUnit != null ? measuringUnit : MeasuringUnit.PCE;

        return InvoiceLine.builder()
                .identifier(identifier)
                .name(name)
                .measuringUnit(munit)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineDiscount(lineDiscount)
                .taxCategory(taxCategory)
                .exemptionReasonCode(exemptionReasonCode)
                .exemptionReasonText(exemptionReasonText)
                .netPrice(netPrice)
                .taxAmount(taxAmount)
                .lineTotalInclusive(lineTotalInclusive)
                .build();
    }
}
