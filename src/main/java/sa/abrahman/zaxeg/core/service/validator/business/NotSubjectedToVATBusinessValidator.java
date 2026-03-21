package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - Services outside scope of tax / Not subject to VAT (BR-O):
 * Rules for the invoicing fields that must hold true when line items are
 * Services outside scope of tax / Not subject to VAT
 */
@Service(InvoiceValidatorBeanNameResolver.NOT_SUBJECTED_TO_VAT_BUSINESS_VALIDATOR)
public class NotSubjectedToVATBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationCommand payload) {
        // TODO Auto-generated method stub
    }
}
