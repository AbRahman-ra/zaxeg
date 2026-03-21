package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - Code Lists (BR-S): List of general codes that are used
 * inside field lists
 */
@Service(InvoiceValidatorBeanNameResolver.CODE_LISTS_BUSINESS_VALIDATOR)
public class CodeListsBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
    }
}
