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

        String rule15 = "BR-15: An Invoice shall have the Amount due for payment";
        StringValueValidator.check(invoice.getFinancials().getPayableAmount().toString(), f).exists(rule15);

        String rule16 = "BR-16: An Invoice must have at least one line item";
        String rule21 = "BR-21: Each Invoice line shall have an Invoice line identifier";
        String rule22 = "BR-22: Each Invoice line shall have an Invoiced quantity";
        String rule24 = "BR-24: Each Invoice line shall have an Invoiced line net amount";
        String rule25 = "BR-25: Each Invoice line shall contain the Item name";
        String rule26 = "BR-26: Each Invoice line shall contain the Item net price";
        CollectionValueValidator.check(invoice.getLines(), f)
                .hasAtleast(1, rule16)
                .allMatch((l) -> l.getIdentifier() != null && !l.getIdentifier().isBlank(), rule21)
                .allMatch((l) -> l.getQuantity() != null, rule22)
                .allMatch((l) -> l.getUnitPrice() != null, rule24)
                .allMatch((l) -> l.getName() != null && !l.getName().isBlank(), rule25)
                .allMatch((l) -> l.getNetPrice() != null, rule26);

        // BR-31: Each Document level allowance shall have a Document level allowance amount
        // BR-32: Each Document level allowance shall have a Document level allowance VAT category code
        // BR-36: Each Document level charge shall have a Document level charge amount
        // BR-37: Each Document level charge shall have a Document level charge VAT category code
        // BR-41: Each Invoice line allowance shall have an Invoice line allowance amount
        // BR-43: Each Invoice line charge shall have an Invoice line charge amount
        // BR-45: Each VAT breakdown shall have a VAT category taxable amount
        // BR-46: Each VAT breakdown shall have a VAT category tax amount
        // BR-47: Each VAT breakdown shall be defined through a VAT category code
        // BR-48: Each VAT breakdown shall have a VAT category rate, except if the Invoice is not subject to VAT
        // BR-49: A Payment instruction shall specify the Payment means type code
        // BR-53: If the VAT accounting currency code is present, then the Invoice total VAT amount in accounting currency shall be provided
        // BR-55: Each Preceding Invoice reference shall contain a Preceding Invoice reference

        validateAdditionalBusinessRules(invoice);
    }

    protected void validateAdditionalBusinessRules(Invoice invoice) {
    };

    protected abstract void validateSpecificRules(Invoice invoice);
}
