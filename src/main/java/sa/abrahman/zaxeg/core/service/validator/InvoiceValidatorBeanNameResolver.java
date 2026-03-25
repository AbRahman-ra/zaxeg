package sa.abrahman.zaxeg.core.service.validator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InvoiceValidatorBeanNameResolver {
    public static final String FULL_INVOICE_VALIDATOR = "zatcaFullInvoiceValidator";

    public static final String METADATA_VALIDATOR = "invoiceMetadataValidator";
    public static final String PARTIES_VALIDATOR = "invoicePartiesValidator";
    public static final String LINES_VALIDATOR = "invoiceLinesValidator";
    public static final String CHECKOUT_VALIDATOR = "invoiceCheckoutValidator";
}
