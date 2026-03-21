package sa.abrahman.zaxeg.core.service.validator;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;

/**
 * UBL invoice validator, as per <a href=
 * "https://zatca.gov.sa/ar/E-Invoicing/SystemsDevelopers/Documents/20230519_ZATCA_Electronic_Invoice_XML_Implementation_Standard_%20vF.pdf">ZATCA
 * Electronic Invoice XML Implementation Standard [2023-05-19]</a>
 *
 */
public abstract class AbstractUBLInvoiceValidator implements InvoiceValidator {

    @Override
    public void validate(InvoiceGenerationCommand payload) throws InvoiceRuleViolationException {
        validateBusinessRules(payload);
        validateSpecificRules(payload);
    }

    protected void validateBusinessRules(InvoiceGenerationCommand invoice) {
        // Function<String, RuntimeException> f = InvoiceRuleViolationException::new;

        // if (invoice.getDocumentAllowanceCharges() != null) {
        //     String rule31Or36 = "BR-31/BR-36: Each Document level allowance/charge shall have a Document level allowance/charge amount";
        //     String rule32Or37 = "BR-32/BR-37: Each Document level allowance/charge shall have a Document level allowance/charge VAT category code";
        //     CollectionValueValidator.check(invoice.getDocumentAllowanceCharges(), f)
        //             .allMatch((ac) -> ac.getAmount() != null, rule31Or36)
        //             .allMatch((ac) -> ac.getTaxCategory() != null, rule32Or37);
        // }

        // if (invoice.getTaxCurrency() != null) {
        //     String rule53 = "BR-53: If the VAT accounting currency code is present, then the Invoice total VAT amount in accounting currency shall be provided";
        //     ObjectValueValidator.check(invoice.getTaxCurrency(), f)
        //             .matches((c) -> c.equals(invoice.getFinancials().getTotalTaxAmountInAccountingCurrency()), rule53);
        // }

        // if (invoice.getBillingReference() != null) {
        //     String rule55 = "BR-55: Each Preceding Invoice reference shall contain a Preceding Invoice reference";
        //     StringValueValidator.check(invoice.getBillingReference().getOriginalInvoiceNumber(), f).exists(rule55);
        // }

        // validateAdditionalBusinessRules(invoice);
    }

    protected void validateAdditionalBusinessRules(InvoiceGenerationCommand invoice) {
    };

    protected abstract void validateSpecificRules(InvoiceGenerationCommand invoice);
}
