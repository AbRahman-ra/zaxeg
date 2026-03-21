package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.CONDITIONS_BUSINESS_VALIDATOR)
public class ConditionsBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationCommand payload) {
        // TODO Auto-generated method stub
    }
}
