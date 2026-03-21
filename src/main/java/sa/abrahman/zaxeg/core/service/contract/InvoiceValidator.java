package sa.abrahman.zaxeg.core.service.contract;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;

/**
 * Invoice validator is not a port, but an internal abstraction layer between
 * {@link sa.abrahman.zaxeg.core.port.in.InvoiceGenerator InvoiceGenerator} and
 * {@link sa.abrahman.zaxeg.core.port.out.InvoiceFormatter InvoiceFormatter}
 */
public interface InvoiceValidator {
    /**
     * @param invoice
     * @throws InvoiceRuleViolationException if the invoice is invalid
     */
    void validate(InvoiceGenerationPayload payload);
}
