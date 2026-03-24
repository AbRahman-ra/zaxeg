package sa.abrahman.zaxeg.core.model.invoice.line;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.common.TaxCategory;

@Getter
@Builder
public class InvoiceLineItem {
    /** BG-31, BT-157: The description of goods or services as per Article 53 of the VAT Implementing Regulation. */
    private String name;

    /** BG-31, BT-156: An identifier, assigned by the Buyer, for the item. */
    private ItemPartyIdentifier itemBuyerIdentifier;

    /** BG-31, BT-155: An identifier, assigned by the Buyer, for the item. */
    private ItemPartyIdentifier itemSellerIdentifier;

    /**
     * <ul>
     * <li>BG-31, BT-157:
     * <ul>
     * <li>An item identifier based on a registered scheme.</li>
     * <li>This should include the product code type and the actual code. This list includes UPC (11 digit, 12 digit, 13 digit EAN), GTIN (14 digit), Customs HS Code and multiple other codes</li>
     * </ul>
     * </li>
     * </ul>
     */
    private ItemPartyIdentifier itemStandardIdentifier;

    /**
     * <ul>
     * <li>BG-30, BT-151: The VAT category code for the invoiced item.</li>
     * <li>BG-30, BT-152: The VAT rate, represented as percentage that applies to the invoiced item as per Article 53 of the VAT Implementing Regulation</li>
     * <li>BG-30, KSA-21: Mandatory element. Use “VAT”</li>
     * </ul>
     */
    private TaxCategory classifiedTaxCategory;
}
