package sa.abrahman.zaxeg.core.generate.config;

public class ValidatorsOrderRegistry {
    // metadata validations
    public static final int METADATA_NUMBER = 1;
    public static final int METADATA_UUID = 2;
    public static final int METADATA_DATE_EXISTS = 3;
    public static final int METADATA_DATE_PRESENT = 4;
    public static final int METADATA_TIME = 5;
    public static final int METADATA_DOCUMENT_TYPE = 6;
    public static final int METADATA_TRANSACTION_CODE_EXISTS = 7;
    public static final int METADATA_TRANSACTION_CODE_COMPLIANT = 8;
    public static final int METADATA_INV_CURRENCY = 9;
    public static final int METADATA_TAX_CURRENCY_EXISTS = 10;
    public static final int METADATA_TAX_CURRENCY_SAR = 11;
    public static final int METADATA_BILLING_REF_ID_EXISTS = 12;
    public static final int METADATA_ISSUANCE_REASONS = 13;
    public static final int METADATA_SUPPLY_DATE = 14;
    public static final int METADATA_SUPPLY_AND_END_DATES = 15;
    public static final int METADATA_SUPPLY_END_DATE = 16;
    public static final int METADATA_SUPPLY_AND_END_DATES_02 = 17;


    /* This utility class should not be instantiated */
    private ValidatorsOrderRegistry() {}
}
