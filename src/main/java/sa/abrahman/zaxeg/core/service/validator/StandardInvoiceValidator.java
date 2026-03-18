package sa.abrahman.zaxeg.core.service.validator;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.ObjectValueValidator;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;

@Component("STANDARD_INVOICE_VALIDATOR")
public class StandardInvoiceValidator extends AbstractZATCAInvoiceValidator {
    @Override
    protected void validateAdditionalBusinessRules(Invoice invoice) {
        Function<String, RuntimeException> f = InvoiceRuleViolationException::new;

        String rule10 = "BR-10: An Invoice shall contain the Buyer postal address. Not applicable for simplified tax invoices and associated credit notes and debit notes";
        ObjectValueValidator.check(invoice.getBuyer(), f).exists(rule10);
    }
}
