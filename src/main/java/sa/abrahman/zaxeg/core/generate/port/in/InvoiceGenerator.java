package sa.abrahman.zaxeg.core.generate.port.in;

import sa.abrahman.zaxeg.core.generate.domain.exception.*;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.shared.exception.BusinessDomainViolationException;

public interface InvoiceGenerator {
    /**
     * @param payload
     * @return the generated invoice as string
     * @throws InvoiceRuleViolationException    for e-invoice implementation violations as described in as per <a href=
     *                                          "https://zatca.gov.sa/ar/E-Invoicing/SystemsDevelopers/Documents/20230519_ZATCA_Electronic_Invoice_XML_Implementation_Standard_%20vF.pdf">ZATCA
     *                                          Electronic Invoice XML Implementation Standard [2023-05-19]</a>
     * @throws BusinessDomainViolationException for other Business Validation Errors
     */
    String handle(InvoiceGenerationPayload payload);
}
