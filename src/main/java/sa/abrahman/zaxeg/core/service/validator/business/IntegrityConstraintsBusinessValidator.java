package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - Conditions (BR-CO): The conditions of each field and its
 * contents
 */
@Service(InvoiceValidatorBeanNameResolver.INTEGRITY_CONSTRAINTS_BUSINESS_VALIDATOR)
public class IntegrityConstraintsBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
    }
}
