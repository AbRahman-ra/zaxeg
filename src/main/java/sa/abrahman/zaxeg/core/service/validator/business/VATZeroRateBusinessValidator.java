package sa.abrahman.zaxeg.core.service.validator.business;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

/**
 * Business Rules - VAT Zero Rate (BR-Z): Rules for the invoicing fields that
 * must hold true when line items have a zero VAT rate
 */
@Service(InvoiceValidatorBeanNameResolver.VAT_ZERO_RATE_BUSINESS_VALIDATOR)
public class VATZeroRateBusinessValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
    }
}
