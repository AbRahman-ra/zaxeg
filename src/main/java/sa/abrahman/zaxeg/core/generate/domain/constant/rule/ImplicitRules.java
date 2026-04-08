package sa.abrahman.zaxeg.core.generate.domain.constant.rule;

import lombok.experimental.UtilityClass;

@UtilityClass
/** General, non-business validation rules */
public class ImplicitRules {
    public static final String DOC_REF_ID_NOT_NULL = "[DOC_REF_ID_NOT_NULL]: Once provided, the document refernce id cannot be null";
    public static final String ITEM_PARTY_ID_NOT_NULL = "[ITEM_PARTY_ID_NOT_NULL]: Once provided, the invoice line item's party's identifier cannot be null";
    public static final String LINE_VAT_AMOUNT_NOT_NULL = "[LINE_VAT_AMOUNT_NOT_NULL]: line VAT amounts ({\"taxAmount\": ..., \"roundingAmount\": ...}) cannot be null";
    public static final String LINE_ITEM_NOT_NULL = "[LINE_ITEM_NOT_NULL]: line Item data ({\"name\": ..., \"classifiedTaxCategory\": {...}, \"itemBuyerIdentifier\"?: {...}, \"itemSellerIdentifier\"?: {...}, \"itemStandardIdentifier\"?: {...}}) cannot be null";
    public static final String LINE_PRICE_NOT_NULL = "[LINE_PRICE_NOT_NULL]: line Price data ({\"amount\": ..., \"quantity\": {...}, \"allowanceOrCharge\"?: {...}}) cannot be null";
    public static final String TAX_AMOUNT_NOT_NULL = "[TAX_AMOUNT_NOT_NULL]: taxAmount cannot be null";
    public static final String TAXABLE_AMOUNT_NOT_NULL = "[TAXABLE_AMOUNT_NOT_NULL]: taxableAmount cannot be null";
}
