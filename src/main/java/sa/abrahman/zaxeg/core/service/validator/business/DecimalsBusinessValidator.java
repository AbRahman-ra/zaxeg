package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - Decimals (BR-S): Rules governing decimals within line item
 * details
 */
@Service(InvoiceValidatorBeanNameResolver.DECIMALS_BUSINESS_VALIDATOR)
public class DecimalsBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationCommand payload) {
        // TODO Auto-generated method stub
    }
}
