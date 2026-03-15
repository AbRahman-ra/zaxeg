package sa.abrahman.zaxeg.core.service.validator;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.model.Invoice;

public interface InvoiceValidator {
    /**
     * @param invoice
     * @throws InvoiceRuleViolationException if the invoice is invalid
     */
    void validate(Invoice invoice);
}
