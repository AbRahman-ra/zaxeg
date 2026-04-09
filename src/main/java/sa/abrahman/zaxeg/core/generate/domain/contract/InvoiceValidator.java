package sa.abrahman.zaxeg.core.generate.domain.contract;

import sa.abrahman.zaxeg.core.generate.domain.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;

/**
 * Invoice validator is not a port, but an internal abstraction layer
 */
public interface InvoiceValidator {
    /**
     * @param invoice
     * @throws InvoiceRuleViolationException if the invoice is invalid
     */
    void validate(InvoiceGenerationPayload payload);
}
