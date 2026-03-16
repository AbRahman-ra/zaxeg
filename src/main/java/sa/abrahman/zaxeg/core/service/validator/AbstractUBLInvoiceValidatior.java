package sa.abrahman.zaxeg.core.service.validator;

import java.util.function.Function;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.CollectionValueValidator;
import sa.abrahman.zaxeg.core.helper.DateValueValidator;
import sa.abrahman.zaxeg.core.helper.ObjectValueValidator;
import sa.abrahman.zaxeg.core.helper.StringValueValidator;
import sa.abrahman.zaxeg.core.model.Invoice;

public abstract class AbstractUBLInvoiceValidatior implements InvoiceValidator {

    @Override
    public void validate(Invoice invoice) {
        validateBusinessRules(invoice);
        validateSpecificRules(invoice);
    }

    protected void validateBusinessRules(Invoice invoice) {
        Function<String, RuntimeException> f = InvoiceRuleViolationException::new;

        String rule02 = "BR-02: An Invoice shall have an Invoice number";
        StringValueValidator.check(invoice.getInvoiceNumber(), f).exists(rule02);

        String rule03 = "BR-03: An Invoice shall have an Invoice issue date";
        DateValueValidator.check(invoice.getIssueDate(), f).exists(rule03);

        String rule04 = "BR-04: An Invoice shall have an Invoice type code";
        ObjectValueValidator.check(invoice.getInvoiceSubtype(), f).exists(rule04);
        ObjectValueValidator.check(invoice.getInvoiceDocumentType(), f).exists(rule04);

        String rule05 = "BR-05: An Invoice shall have an Invoice currency code";
        ObjectValueValidator.check(invoice.getDocumentCurrency(), f).exists(rule05);

        String rule06 = "BR-06: An Invoice shall contain the Seller name";
        ObjectValueValidator.check(invoice.getSupplier(), f).exists(rule06);
        StringValueValidator.check(invoice.getSupplier().getRegistrationName(), f).exists(rule06);

        String rule08 = "BR-08: An Invoice shall contain the Seller postal address";
        ObjectValueValidator.check(invoice.getSupplier().getAddress(), f).exists(rule08);

        String rule09 = "BR-09: The Seller postal address shall contain a Seller country code";
        ObjectValueValidator.check(invoice.getSupplier().getAddress().getCountry(), f).exists(rule09);

        /**
         * Rules 13 & 14 are always valid since we implemented sum calculator on the
         * {@link sa.abrahman.zaxeg.core.model.Invoice} domain model level
         */

        String rule15 = "An Invoice shall have the Amount due for payment";
        StringValueValidator.check(invoice.getFinancials().getPayableAmount().toString(), f).exists(rule15);

        String rule16 = "BR-16: An Invoice must have at least one line item";
        CollectionValueValidator.check(invoice.getLines(), f).hasAtleast(1, rule16);

        // BR-CO-04: Invoice lines must have valid tax categories

        validateAdditionalBusinessRules(invoice);
    }

    protected void validateAdditionalBusinessRules(Invoice invoice) {
    };

    protected abstract void validateSpecificRules(Invoice invoice);
}
