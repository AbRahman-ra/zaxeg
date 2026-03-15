package sa.abrahman.zaxeg.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvoiceLine {
    // meta data
    private String identifier; // e.g., "SKU-12345"
    private String name; // e.g., "Spring Boot Consulting"

    @Builder.Default
    private TaxCategory taxCategory = TaxCategory.STANDARD;

    // Base Values
    private BigDecimal quantity;
    private BigDecimal unitPrice; // Price strictly BEFORE tax and discounts
    private BigDecimal lineDiscount; // Discount applied to this specific line

    // Calculated Values (Validated by ZATCA BR-KSA-51 & BR-KSA-52)
    private BigDecimal netPrice; // (quantity * unitPrice) - lineDiscount
    private BigDecimal taxAmount; // netPrice * (taxCategory.rate / 100)
    private BigDecimal lineTotalInclusive; // netPrice + taxAmount

    public static InvoiceLine create(String identifier, String name, TaxCategory taxCategory, BigDecimal quantity,
            BigDecimal unitPrice, BigDecimal lineDiscount) {
        if (taxCategory == null) taxCategory = TaxCategory.STANDARD;

        BigDecimal rawNetPrice = quantity.multiply(unitPrice).subtract(lineDiscount);
        BigDecimal netPrice = rawNetPrice.setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxMultiplier = taxCategory.getRate().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = netPrice.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);

        BigDecimal lineTotalInclusive = netPrice.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        return InvoiceLine.builder()
                .identifier(identifier)
                .name(name)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineDiscount(lineDiscount)
                .taxCategory(taxCategory)
                .netPrice(netPrice)
                .taxAmount(taxAmount)
                .lineTotalInclusive(lineTotalInclusive)
                .build();
    }
}
