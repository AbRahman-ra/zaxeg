package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.GENERIC_BUSINESS_VALIDATOR)
public class GenericInvoiceBusinessValidator implements InvoiceValidator {
    private final InvoiceValidator integrityConstraints;

    public GenericInvoiceBusinessValidator(
            @Qualifier(InvoiceValidatorBeanNameResolver.INTEGRITY_CONSTRAINTS_BUSINESS_VALIDATOR) InvoiceValidator integrityConstraints) {
        this.integrityConstraints = integrityConstraints;
    }

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        integrityConstraints.validate(payload);
        // TODO add rest of the validators
    }
}
