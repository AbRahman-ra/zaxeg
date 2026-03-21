package sa.abrahman.zaxeg.core.service.validator;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;

public interface InvoiceValidator {
    /**
     * @param invoice
     * @throws InvoiceRuleViolationException if the invoice is invalid
     */
    void validate(InvoiceGenerationCommand payload);
}
