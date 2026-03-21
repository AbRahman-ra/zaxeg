package sa.abrahman.zaxeg.core.service.validator;

public class InvoiceValidatorBeanNameResolver {
    /**
     * Container validator that wraps business and KSA rules
     */
    public static final String FULL_INVOICE_VALIDATOR = "zatcaFullInvoiceValidator";

    // business validators
    /**
     * Container validator that wraps required business rules
     */
    public static final String GENERIC_BUSINESS_VALIDATOR = "genericInvoiceBusinessValidator";
    public static final String INTEGRITY_CONSTRAINTS_BUSINESS_VALIDATOR = "integrityConstraintsBusinessValidator";
    public static final String CONDITIONS_BUSINESS_VALIDATOR = "conditionsBusinessValidator";
    public static final String VAT_STANDARD_RATE_BUSINESS_VALIDATOR = "vatStandardRateBusinessValidator";
    public static final String VAT_ZERO_RATE_BUSINESS_VALIDATOR = "vatZeroRateBusinessValidator";
    public static final String EXEMPTED_FROM_VAT_BUSINESS_VALIDATOR = "exemptedFromVatBusinessValidator";
    public static final String NOT_SUBJECTED_TO_VAT_BUSINESS_VALIDATOR = "notSubjectedToVatBusinessValidator";
    public static final String CODE_LISTS_BUSINESS_VALIDATOR = "codeListsBusinessValidator";
    public static final String DECIMALS_BUSINESS_VALIDATOR = "decimalsBusinessValidator";

    // ksa validators
    /**
     * Container validator that wraps required KSA rules
     */
    public static final String GENERIC_KSA_VALIDATOR = "genericInvoiceKSAValidator";
    private InvoiceValidatorBeanNameResolver() {}
}
