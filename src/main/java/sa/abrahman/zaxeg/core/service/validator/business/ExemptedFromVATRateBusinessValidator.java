package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - Exempted from VAT (BR-E): Rules for the invoicing fields
 * that must hold true when line items are exempt from VAT
 */
@Service(InvoiceValidatorBeanNameResolver.EXEMPTED_FROM_VAT_BUSINESS_VALIDATOR)
public class ExemptedFromVATRateBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationCommand payload) {
        // TODO Auto-generated method stub
    }
}
