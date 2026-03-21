package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - Conditions (BR-CO): The conditions of each field and its contents
 */
@Service(InvoiceValidatorBeanNameResolver.INTEGRITY_CONSTRAINTS_BUSINESS_VALIDATOR)
public class IntegrityConstraintsBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationCommand payload) {
        // TODO Auto-generated method stub
    }
}
