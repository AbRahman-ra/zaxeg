package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - VAT Standard Rate (BR-S): Rules for the invoicing fields that must hold true when line items have a standard VAT rate
 */
@Service(InvoiceValidatorBeanNameResolver.VAT_STANDARD_RATE_BUSINESS_VALIDATOR)
public class VATStandardRateBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationCommand payload) {
        // TODO Auto-generated method stub
    }
}
